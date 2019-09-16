import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import Reflux from 'reflux';
import {Button, Col, Row} from 'react-bootstrap';
import {Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import Routes from 'routing/Routes';
import {LinkContainer} from 'react-router-bootstrap';
import 'react-confirm-alert/src/react-confirm-alert.css';
import {FormattedMessage} from 'react-intl';
import {Input} from 'components/bootstrap';
import AlertListActions from "./AlertListActions";
import AlertListStore from "./AlertListStore";

const INIT_LIST = {
    title: '',
    description: '',
    lists: '',
    };

const CreateListFormInput = createReactClass({
        displayName: 'CreateListFormInput',

    mixins: [Reflux.connect(AlertListStore)],

    propTypes: {
        list: PropTypes.object,
        create: PropTypes.bool.isRequired,
        nodes: PropTypes.object,
    },
    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    componentWillMount(){
    },

    componentWillReceiveProps(nextProps) {
        if(!_.isEqual(nextProps.nodes, this.props.nodes)){
           // this._isPluginsPresent();
        }
    },

    getDefaultProps() {
        return {
            list: INIT_LIST,
            default_values: {
                title: '',
                description: '',
                lists: '',
            },
        };
    },

    getInitialState() {
        let list = ObjectUtils.clone(this.props.list);

        if (this.props.create) {
            list.title = this.props.default_values.title;
            list.description = this.props.default_values.description;
            list.lists = this.props.default_values.lists;

        }

        return {
            list: list,
            isModified: false,
            isValid: true,
            contentComponent: <Spinner/>,
        };
    },

    _save() {
        AlertListActions.create.triggerPromise(this.state.list).then((response) => {
            if (response === true) {
                    this.setState({list: list});
            }
        });
        this.setState({isModified: false});
    },

    _update() {
        AlertListActions.update.triggerPromise(this.props.list.title, this.state.list).then((response) => {
            if (response === true) {
                    this.setState({list: list});
            }
        });
        this.setState({isModified: false});
    },

    onSubmitUploadFile(submitEvent) {
        submitEvent.preventDefault();
        if (!this.refs.uploadedFile.files || !this.refs.uploadedFile.files[0]) {
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            this.setState({alertLists: JSON.parse(evt.target.result)});
        };

        reader.readAsText(this.refs.uploadedFile.files[0]);
    },

    _updateConfigField(field, value) {
        const update = ObjectUtils.clone(this.state.list);
        update[field] = value;
        this.setState({ list: update });
    },

    _onUpdate(field) {
        return e => {
            this._updateConfigField(field, e.target.value);
        };
    },

    render: function() {

        let actions;
        const buttonCancel = (
            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                <Button><FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" /></Button>
            </LinkContainer>
        );

        let buttonSave;
        if (this.props.create) {
            buttonSave = (
                <Button onClick={this._save} disabled={!this.state.isValid} className="btn btn-md btn-primary">
                    <FormattedMessage id="wizard.save" defaultMessage="Save"/>
                </Button>
            );
        }
         else {
            buttonSave = (
                <Button onClick={this._update} disabled={!this.state.isValid}
                        className="btn btn-md btn-primary">
                    <FormattedMessage id= "wizard.save" defaultMessage= "Save" />
                </Button>
            );
        }

        actions = (
            <div className="alert-actions pull-left">
                {buttonCancel}{' '}
                {buttonSave}{' '}
            </div>);

        const style = { display: 'flex', alignItems: 'center' };

        return (
            <div>
                <Row>
                    <Col md={4}>
                        <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />}
                               onChange={this._onUpdate('title')}                               name="title" />
                        <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />}
                               onChange={this._onUpdate('description')}
                               name="description"/>
                    </Col>
                </Row>
                <Row style={style}>
                    <Col md={5}>
                    <Input style={{minWidth: 600}} ref="list" id="wizard.fieldList" name="list" type="textarea" rows="10"
                           label={<FormattedMessage id ="wizard.fieldList" defaultMessage="List" />}
                           onChange={this._onUpdate('wizard.fieldList')}/>
                        {actions}
                    </Col>
                    <Col md={3}>
                        <form onSubmit={this.onSubmitUploadFile} className="upload" encType="multipart/form-data">
                                <input ref="uploadedFile" type="file" name="bundle" />
                        </form>
                    </Col>
                </Row>
            </div>
        );
    },
});

export default CreateListFormInput;