/*
 * Copyright (C) 2018 Airbus CyberSecurity (SAS)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package com.airbus_cyber_security.graylog.wizard.alert.business;

import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.google.common.collect.Maps;
import org.graylog.events.conditions.Expr;
import org.graylog.events.conditions.Expression;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationSeries;
import org.graylog2.plugin.streams.StreamRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AlertRuleUtils {

	private static final int MILLISECONDS_IN_A_MINUTE = 60 * 1000;
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleUtils.class);
	public static final String GROUPING_FIELDS = "grouping_fields";
	public static final String DISTINCTION_FIELDS = "distinction_fields";
	public static final String TIME = "time";
	public static final String GRACE = "grace";
	public static final String ADDITIONAL_THRESHOLD = "additional_threshold";
	public static final String ADDITIONAL_THRESHOLD_TYPE = "additional_threshold_type";
    public static final String THRESHOLD_TYPE = "threshold_type";
    public static final String THRESHOLD = "threshold";
    public static final String SEVERITY = "severity";
    public static final String LOG_BODY = "log_body";
    public static final String SPLIT_FIELDS = "split_fields";
    public static final String AGGREGATION_TIME = "aggregation_time";
	public static final String ALERT_TAG = "alert_tag";
	public static final String SINGLE_NOTIFICATION = "single_notification";

    public static final String COMMENT_ALERT_WIZARD = "Generated by the alert wizard";

    private double getThreshold(Expression<Boolean> expression){
		Expression<Double> expressionRight;
		if (expression instanceof Expr.Greater) {
			expressionRight= ((Expr.Greater) expression).right();
		} else if (expression instanceof Expr.GreaterEqual) {
			expressionRight= ((Expr.GreaterEqual) expression).right();
		} else if (expression instanceof Expr.Lesser) {
			expressionRight= ((Expr.Lesser) expression).right();
		} else if (expression instanceof Expr.LesserEqual) {
			expressionRight= ((Expr.LesserEqual) expression).right();
		} else if (expression instanceof Expr.Equal) {
			expressionRight= ((Expr.Equal) expression).right();
		} else {
			LOG.error("Can't get threshold, error cast Expression");
			return 0;
		}

		if (expressionRight instanceof Expr.NumberValue) {
			return ((Expr.NumberValue) expressionRight).value();
		} else {
			LOG.error("Can't get threshold, error cast Right Expression");
			return 0;
		}
	}

	// TODO should avoid these conversions by always working with ms (from the IHM down to the server)
	private long convertMillisecondsToMinutes(long value) {
		return value / MILLISECONDS_IN_A_MINUTE;
	}

	public long convertMinutesToMilliseconds(long value) {
		return value * MILLISECONDS_IN_A_MINUTE;
	}

	public Map<String, Object> getConditionParameters(EventProcessorConfig eventConfig){
		Map<String, Object> parametersCondition = Maps.newHashMap();

		switch (eventConfig.type()) {
			case "correlation-count":
				CorrelationCountProcessorConfig correlationConfig = (CorrelationCountProcessorConfig) eventConfig;
				parametersCondition.put(THRESHOLD, correlationConfig.threshold());
				parametersCondition.put(THRESHOLD_TYPE, correlationConfig.thresholdType());
				parametersCondition.put(ADDITIONAL_THRESHOLD, correlationConfig.additionalThreshold());
				parametersCondition.put(ADDITIONAL_THRESHOLD_TYPE, correlationConfig.additionalThresholdType());
				parametersCondition.put(TIME, this.convertMillisecondsToMinutes(correlationConfig.searchWithinMs()));
				parametersCondition.put(GROUPING_FIELDS, correlationConfig.groupingFields());
				parametersCondition.put(GRACE, this.convertMillisecondsToMinutes(correlationConfig.executeEveryMs()));
				break;
			case "aggregation-v1":
				AggregationEventProcessorConfig aggregationConfig = (AggregationEventProcessorConfig) eventConfig;
				parametersCondition.put(TIME, this.convertMillisecondsToMinutes(aggregationConfig.searchWithinMs()));
				parametersCondition.put(GRACE, this.convertMillisecondsToMinutes(aggregationConfig.executeEveryMs()));
				parametersCondition.put(THRESHOLD, getThreshold(aggregationConfig.conditions().get().expression().get()));
				parametersCondition.put(THRESHOLD_TYPE, aggregationConfig.conditions().get().expression().get().expr());
				AggregationSeries series = aggregationConfig.series().get(0);
				// TODO should introduce constants here for "type" and "field"...
				parametersCondition.put("type", series.function().toString());
				List<String> distinctFields = new ArrayList<>();
				Optional<String> seriesField = series.field();
				if (seriesField.isPresent()) {
					// TODO think about this, but there is some code smell here...
					// It is because AggregationEventProcessorConfig is used both for Count and Statistical conditions
					String distinctField = seriesField.get();
					parametersCondition.put("field", distinctField);
					distinctFields.add(distinctField);
				}
				parametersCondition.put(GROUPING_FIELDS, aggregationConfig.groupBy());
				parametersCondition.put(DISTINCTION_FIELDS, distinctFields);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		return parametersCondition;
	}
	
    public List<FieldRule> getListFieldRule(List<StreamRule> listStreamRule) {
         List<FieldRule> listFieldRule = new ArrayList<>();
         for (StreamRule streamRule: listStreamRule) {
             if (streamRule.getInverted()) {
                 listFieldRule.add(FieldRule.create(streamRule.getId(), streamRule.getField(), -streamRule.getType().toInteger(), streamRule.getValue()));
             } else {
                 listFieldRule.add(FieldRule.create(streamRule.getId(), streamRule.getField(), streamRule.getType().toInteger(), streamRule.getValue()));
             }
         }
         return listFieldRule;
    }

	public Map<String, Object> getNotificationParameters(LoggingNotificationConfig loggingNotificationConfig){
		Map<String, Object> parametersNotification = Maps.newHashMap();
		parametersNotification.put(SEVERITY, loggingNotificationConfig.severity());
		parametersNotification.put(LOG_BODY, loggingNotificationConfig.logBody());
		parametersNotification.put(SPLIT_FIELDS, loggingNotificationConfig.splitFields());
		parametersNotification.put(AGGREGATION_TIME, loggingNotificationConfig.aggregationTime());
		parametersNotification.put(ALERT_TAG, loggingNotificationConfig.alertTag());
		parametersNotification.put(SINGLE_NOTIFICATION, loggingNotificationConfig.singleMessage());
		return parametersNotification;
	}

	// TODO remove this method => should have a more regular code (empty lists instead of null)!!!
	public <T> Collection<T> nullSafe(Collection<T> c) {
		return (c == null) ? Collections.<T>emptyList() : c;
	}

}
