package com.airbus_cyber_security.graylog.alert.utilities;

import com.airbus_cyber_security.graylog.alert.FieldRule;
import com.airbus_cyber_security.graylog.alert.FieldRuleImpl;
import com.google.common.collect.Maps;
import org.graylog2.alerts.AbstractAlertCondition;
import org.graylog2.plugin.streams.StreamRule;

import java.util.*;

public class AlertRuleUtils {

	public static final String GROUPING_FIELDS = "grouping_fields";
	public static final String DISTINCTION_FIELDS = "distinction_fields";
	public static final String TIME = "time";
	public static final String GRACE = "grace";
	public static final String BACKLOG = "backlog";
	public static final String ADDITIONAL_STREAM = "additional_stream";
	public static final String ADDITIONAL_THRESHOLD = "additional_threshold";
	public static final String ADDITIONAL_THRESHOLD_TYPE = "additional_threshold_type";
	public static final String MESSAGES_ORDER = "messages_order";
    public static final String THRESHOLD_TYPE = "threshold_type";
    public static final String THRESHOLD = "threshold";
    public static final String MAIN_THRESHOLD_TYPE = "main_threshold_type";
    public static final String MAIN_THRESHOLD = "main_threshold";
    public static final String SEVERITY = "severity";
    public static final String CONTENT = "content";
    public static final String SPLIT_FIELDS = "split_fields";
    public static final String AGGREGATION_TIME = "aggregation_time";
    public static final String LIMIT_OVERFLOW = "limit_overflow";
    public static final String COMMENT = "comment";
    public static final String REPEAT_NOTIFICATION = "repeat_notifications";
    
    public static final String COMMENT_ALERT_WIZARD = "Generated by the alert wizard";
   
    public static final String TYPE_LOGGING_ALERT = "com.airbus_cyber_security.graylog.LoggingAlert";
    public static final String TYPE_CORRELATION = "com.airbus_cyber_security.graylog.CorrelationCount";
    public static final String TYPE_AGGREGATION = "com.airbus_cyber_security.graylog.AggregationCount";

	public Map<String, Object> getConditionParameters(String streamID, String alertRuleCondType, Map<String, Object> alertRuleCondParameters){
    	
    	Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(GRACE, alertRuleCondParameters.getOrDefault(GRACE, 0));
    	parameters.put(BACKLOG, alertRuleCondParameters.getOrDefault(BACKLOG, 1000));
    	parameters.put(TIME, alertRuleCondParameters.getOrDefault(TIME, 5));
    	parameters.put(THRESHOLD, alertRuleCondParameters.getOrDefault(THRESHOLD, 0));
    	parameters.put(THRESHOLD_TYPE, alertRuleCondParameters.get(THRESHOLD_TYPE));
    	parameters.put(REPEAT_NOTIFICATION, alertRuleCondParameters.getOrDefault(REPEAT_NOTIFICATION, false));

    	switch (alertRuleCondType) {
    	case "STATISTICAL":
    		parameters.put("type", alertRuleCondParameters.get("type"));
    		parameters.put("field", alertRuleCondParameters.get("field"));
    		break;
    	case "GROUP_DISTINCT":
    		parameters.put(GROUPING_FIELDS, alertRuleCondParameters.getOrDefault(GROUPING_FIELDS, Collections.emptyList()));
    		parameters.put(DISTINCTION_FIELDS, alertRuleCondParameters.getOrDefault(DISTINCTION_FIELDS, Collections.emptyList()));
    		parameters.put(COMMENT, COMMENT_ALERT_WIZARD);
    		break;
    	case "THEN":
    		parameters.put(ADDITIONAL_STREAM, streamID);
    		parameters.put(MESSAGES_ORDER, "AFTER");
    		parameters.put(MAIN_THRESHOLD, parameters.remove(THRESHOLD));
    		parameters.put(MAIN_THRESHOLD_TYPE, parameters.remove(THRESHOLD_TYPE));
    		parameters.put(ADDITIONAL_THRESHOLD, alertRuleCondParameters.getOrDefault(ADDITIONAL_THRESHOLD, 0));
        	parameters.put(ADDITIONAL_THRESHOLD_TYPE, alertRuleCondParameters.getOrDefault(ADDITIONAL_THRESHOLD_TYPE, "MORE"));
        	parameters.put(GROUPING_FIELDS, alertRuleCondParameters.getOrDefault(GROUPING_FIELDS, Collections.emptyList()));
        	parameters.put(COMMENT, COMMENT_ALERT_WIZARD);
    		break;
    	case "AND":
    		parameters.put(ADDITIONAL_STREAM, streamID);
    		parameters.put(MESSAGES_ORDER, "ANY");
    		parameters.put(MAIN_THRESHOLD, parameters.remove(THRESHOLD));
    		parameters.put(MAIN_THRESHOLD_TYPE, parameters.remove(THRESHOLD_TYPE));
    		parameters.put(ADDITIONAL_THRESHOLD, alertRuleCondParameters.getOrDefault(ADDITIONAL_THRESHOLD, 0));
        	parameters.put(ADDITIONAL_THRESHOLD_TYPE, alertRuleCondParameters.getOrDefault(ADDITIONAL_THRESHOLD_TYPE, "MORE"));
        	parameters.put(GROUPING_FIELDS, alertRuleCondParameters.getOrDefault(GROUPING_FIELDS, Collections.emptyList()));
        	parameters.put(COMMENT, COMMENT_ALERT_WIZARD);
    		break;

    	default:
    		break;
    	}
    	
    	return parameters;
    }
	
    public String getGraylogConditionType(String alertRuleConditionType) {
    	String conditionType;
        switch (alertRuleConditionType) {
		case "STATISTICAL":
			conditionType = AbstractAlertCondition.Type.FIELD_VALUE.toString();
			break;
		case "GROUP_DISTINCT":
			conditionType = TYPE_AGGREGATION;
			break;
		case "THEN":
			conditionType = TYPE_CORRELATION;
			break;
		case "AND":
			conditionType = TYPE_CORRELATION;
			break;
			
		default:
			conditionType = AbstractAlertCondition.Type.MESSAGE_COUNT.toString();
			break;
		}
        return conditionType;
    }
    
    public List<FieldRuleImpl> getListFieldRule(List<StreamRule> listStreamRule) {
         List<FieldRuleImpl> listFieldRule = new ArrayList<>();
         for (StreamRule streamRule: listStreamRule) {
             if(streamRule.getInverted()){
                 listFieldRule.add(FieldRuleImpl.create(streamRule.getId(), streamRule.getField(), -streamRule.getType().toInteger(), streamRule.getValue()));
             }else{
                 listFieldRule.add(FieldRuleImpl.create(streamRule.getId(), streamRule.getField(), streamRule.getType().toInteger(), streamRule.getValue()));
             }
         }
         return listFieldRule;
    }
    
	public boolean isValidSeverity(String severity) {
		return  (severity.equals("info") || severity.equals("low") ||
				severity.equals("medium") || severity.equals("high"));
	}

	public <T> Collection<T> nullSafe(Collection<T> c) {
		return (c == null) ? Collections.<T>emptyList() : c;
	}
}
