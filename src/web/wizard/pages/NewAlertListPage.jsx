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

// sources of inspiration for this code: 
// * pages/ShowNodePage.jsx
// * pages/ConfigurationsPage.tsx
// * pages/NodesPage.jsx
import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import { Button, Col, Row } from 'components/bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { DocumentTitle, PageHeader, Spinner } from 'components/common';
import Routes from 'routing/Routes';
import { addLocaleData, IntlProvider, FormattedMessage } from 'react-intl';
import messages_fr from 'translations/fr.json';
import CreateListFormInput from 'wizard/components/lists/CreateListFormInput';
import { CurrentUserStore } from 'stores/users/CurrentUserStore';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
    'fr': messages_fr
};

const NewAlertListPage = createReactClass({
    displayName: 'NewAlertListPage',

    propTypes: {
        location: PropTypes.object.isRequired,
        params: PropTypes.object.isRequired,
        children: PropTypes.element,
    },

    render() {

        return (
            <IntlProvider locale={language} messages={messages[language]}>
                <DocumentTitle title="New list">
                    <div>
                        <PageHeader title={<FormattedMessage id="wizard.newList" defaultMessage="Wizard: New list" />}>
                        <span>
                            <FormattedMessage id="wizard.definelist" defaultMessage="You can define a list." />
                        </span>
                            <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard list in the documentation." />
                        </span>
                            <span>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                <Button bsStyle="info"><FormattedMessage id= "wizard.backlist" defaultMessage= "Back to lists" /></Button>
                            </LinkContainer>
                                &nbsp;
                        </span>
                        </PageHeader>
                        <Row className="content">
                            <Col md={12}>
                               <CreateListFormInput create={true}/>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default NewAlertListPage;
