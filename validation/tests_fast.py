# These tests do not require for the graylog server to be shutdown and started between each of them
# Ideally all tests should be run in this suite, so that the start/shutdown of Graylog is done outside the tests
#
# to create and populate the test venv:
# * python3 -m venv venv
# * source venv/bin/activate
# * pip install -r requirements.txt
# to execute these tests:
# * activate venv
#   source ./venv/bin/activate
# * execute tests
#   python -m unittest --verbose
# To execute only one test, suffix with the fully qualified test name. Example:
#   python -m unittest test.Test.test_create_alert_rule_with_list_should_generate_event_when_message_field_is_in_list

from unittest import TestCase
import time
from graylog import Graylog

_PERIOD = 1

class TestsFast(TestCase):

    @classmethod
    def setUpClass(cls) -> None:
        cls._graylog = Graylog()
        cls._graylog.start()

    @classmethod
    def tearDownClass(cls) -> None:
        cls._graylog.stop()

    def setUp(self):
        self._api = self._graylog.access_rest_api()

    def tearDown(self):
        rules = self._api.get_alert_rules().json()
        for rule in rules:
            self._api.delete_alert_rule(rule['title'])

        lists = self._api.get_lists()
        for list in lists['lists']:
            self._api.delete_list(list['title'])

    def test_get_alerts_should_be_found(self):
        response = self._graylog.get_alert_rules()
        self.assertEqual(200, response.status_code)

    def test_create_alert_rule_should_not_fail(self):
        rule_title = 'alert_rule_title'
        alert_rule = self._graylog.create_alert_rule_count(rule_title, _PERIOD)
        self.assertEqual(rule_title, alert_rule['title'])

    def test_create_alert_rule_statistics_should_not_fail(self):
        rule_title = 'statistics'
        alert_rule = self._graylog.create_alert_rule_statistics(rule_title, _PERIOD)
        self.assertEqual(rule_title, alert_rule['title'])

    def test_get_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        title = 'rule_title'
        self._graylog.create_alert_rule_and(title, _PERIOD)
        retrieved_alert_rule = self._graylog.get_alert_rule(title)
        self.assertEqual('<', retrieved_alert_rule['condition_parameters']['additional_threshold_type'])

    def test_get_alert_rule_should_return_correct_additional_threshold__issue69(self):
        title = 'rule_title'
        self._graylog.create_alert_rule_and(title, _PERIOD, additional_threshold=1)
        retrieved_alert_rule = self._graylog.get_alert_rule(title)
        self.assertEqual(1, retrieved_alert_rule['condition_parameters']['additional_threshold'])

    def test_alert_rule_with_no_conditions_should_trigger__issue139(self):
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        self._graylog.create_alert_rule_count('rule_title', _PERIOD, stream=stream)
        # TODO should create a gelf_input when instantiating graylog and delete it at the send
        #      so that other tests do not fail
        with self._graylog.create_gelf_input() as inputs:
            inputs.send({})
            # we have to wait for the period before the event triggers, then there might be some more processing time
            self._graylog.wait_until_aggregation_event(2*_PERIOD)

    def test_delete_alert_rule_with_no_conditions_should_not_delete_default_stream(self):
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        title = 'rule_title'
        self._graylog.create_alert_rule_count(title, _PERIOD, stream=stream)
        self._api.delete_alert_rule(title)
        default_stream = self._api.get_stream('000000000000000000000001')
        self.assertEqual(200, default_stream.status_code)

    def test_update_alert_rule_with_no_conditions_should_not_fail(self):
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        title = 'rule_title'
        rule = self._graylog.create_alert_rule_count(title, _PERIOD, stream=stream)
        response = self._graylog.update_alert_rule(title, {**rule, 'description': 'new description'})
        self.assertEqual(202, response.status_code)

    def test_update_alert_rule_should_not_raise_exception_when_removing_conditions(self):
        title = 'rule_title'
        rule = self._graylog.create_alert_rule_count(title, _PERIOD)
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        self._graylog.start_logs_capture()
        self._graylog.update_alert_rule(title, {**rule, 'stream': stream})
        logs = self._graylog.extract_logs()
        self.assertNotIn('Exception', logs)

    def test_update_alert_rule_should_delete_stream_when_removing_conditions(self):
        title = 'rule_title'
        rule = self._graylog.create_alert_rule_count(title, _PERIOD)
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        self._graylog.update_alert_rule(title, {**rule, 'stream': stream})
        response = self._api.get_stream(rule['stream']['id'])
        self.assertEqual(404, response.status_code)

    def test_update_alert_rule_should_delete_stream_when_removing_stream_conditions_from_a_rule_with_list(self):
        list_title = 'users'
        self._graylog.create_list(list_title, ['toto', 'tata', 'titi'])
        stream = {
            'field_rule': [{
                'field': 'x',
                'type': 7,
                'value': list_title
            }, {
                'field': 'source',
                'type': 1,
                'value': 'source123'
            }],
            'matching_type': 'AND'
        }
        title = 'A'
        rule = self._graylog.create_alert_rule_count(title, _PERIOD, stream=stream)
        stream = {
            'field_rule': [{
                'field': 'x',
                'type': 7,
                'value': list_title
            }],
            'matching_type': 'AND'
        }
        self._graylog.update_alert_rule(title, {**rule, 'stream': stream})
        response = self._api.get_stream(rule['stream']['id'])
        self.assertEqual(404, response.status_code)

    def test_create_alert_rule_should_set_event_definition_description__issue102(self):
        title = 'aaa'
        alert_rule = self._graylog.create_alert_rule_count(title, _PERIOD, description='rule_description')
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        self.assertEqual('rule_description', event_definition['description'])

    def test_get_alert_rule_should_return_the_description_of_the_event_definition__issue102(self):
        title = 'aaa'
        alert_rule = self._graylog.create_alert_rule_count(title, _PERIOD)
        event_definition = self._graylog.get_event_definition(alert_rule['condition'])
        event_definition['description'] = 'new_description'
        self._graylog.update_event_definition(event_definition)
        alert_rule = self._graylog.get_alert_rule(title)
        self.assertEqual('new_description', alert_rule['description'])

    def test_create_list_should_create_data_adapter(self):
        self._graylog.create_list('test', ['a'])
        response = self._graylog.query_data_adapter('alert-wizard-list-data-adapter-test', 'a')
        self.assertEqual(200, response.status_code)

    def test_create_list_should_create_lookup_table_with_the_list_values(self):
        self._graylog.create_list('test', ['a'])
        response = self._graylog.query_lookup_table('alert-wizard-list-lookup-table-test', 'a')
        result = response.json()['single_value']
        self.assertEqual('a', result)
