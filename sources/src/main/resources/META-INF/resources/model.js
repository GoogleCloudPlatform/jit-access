"use strict"

/** Manage browser-local storage */
class LocalSettings {
    get environment() {
        if (typeof (Storage) !== "undefined") {
            return localStorage.getItem("environment");
        }
        else {
            return null;
        }
    }

    set environment(value) {
        if (typeof (Storage) !== "undefined") {
            localStorage.setItem("environment", value);
        }
    }
}

class Model {
    _getHeaders() {
        return { "X-JITACCESS": "1" };
    }

    _formatError(error) {
        let message = (error.responseJSON && error.responseJSON.message)
            ? error.responseJSON.message
            : "";
        return `${message} (HTTP ${error.status}: ${error.statusText})`;
    }

    get context() {
        console.assert(this._context);
        return this._context;
    }

    async initialize() {
        try {
            await new Promise(r => setTimeout(r, 200));
            this._context = await $.ajax({
                url: "/api/user/context",
                dataType: "json",
                headers: this._getHeaders()
            });
        }
        catch (error) {
            throw this._formatError(error);
        }
    }

    async listEnvironments() {
        try {
            return await $.ajax({
                url: "/api/catalog/environments",
                dataType: "json",
                headers: this._getHeaders()
            });
        }
        catch (error) {
            throw this._formatError(error);
        }
    }

    async listGroups(environment) {
        try {
            return await $.ajax({
                url: `/api/catalog/environments/${environment}/groups`,
                dataType: "json",
                headers: this._getHeaders()
            });
        }
        catch (error) {
            throw this._formatError(error);
        }
    }

    async getGroup(environment, groupId) {
        try {
            return await $.ajax({
                url: `/api/catalog/environments/${environment}/groups/${groupId}`,
                dataType: "json",
                headers: this._getHeaders()
            });
        }
        catch (error) {
            throw this._formatError(error);
        }
    }
}

class DebugModel extends Model {
    constructor() {
        super();
        $("body").append(`
            <div id="debug-pane">
                <div>
                User: <input type="text" id="debug-user"/>
                </div>
                <hr/>
                <div>
                    listEnvironments:
                    <select id="debug-listEnvironments">
                        <option value="">(default)</option>
                        <option value="error">Simulate error</option>
                        <option value="0">Simulate 0 results</option>
                        <option value="10">Simulate 10 result</option>
                        <option value="100">Simulate 100 results</option>
                    </select>
                </div>
                <div>
                    getGroup:
                    <select id="debug-getGroup">
                        <option value="">(default)</option>
                        <option value="error">Simulate error</option>
                        <option value="success">Success</option>
                    </select>
                </div>
            </div>
        `);

        //
        // Persist settings.
        //
        [
            "debug-user",
            "debug-listEnvironments",
            "debug-getGroup"
        ].forEach(setting => {
            $("#" + setting).val(localStorage.getItem(setting))
            $("#" + setting).change(() => {
                localStorage.setItem(setting, $("#" + setting).val());
            });
        });
    }

    _getHeaders() {
        const headers = super._getHeaders();
        const user = $("#debug-user").val();
        if (user) {
            headers["X-debug-principal"] = user;
        }
        return headers;
    }

    async _simulateError() {
        await new Promise(r => setTimeout(r, 1000));
        return Promise.reject("Simulated error");
    }

    async initialize() {
        super._context = {
            subject: {
                email: $("#debug-user").val(),
                principals: [$("#debug-user").val()],
            },
            application: {
                version: '0.0.1'
            }
        };
    }

    async listEnvironments() {
        var setting = $("#debug-listEnvironments").val();
        if (!setting) {
            return super.listEnvironments();
        }
        else if (setting === "error") {
            await this._simulateError();
        }
        else {
            await new Promise(r => setTimeout(r, 2000));
            return Promise.resolve({
                environments: Array.from(
                    { length: setting },
                    (e, i) => {
                        return {
                            name: `environment-${i}`,
                            description: `Debug environment-${i}`
                        };
                    })
            });
        }
    }

    async getGroup(environment, groupId) {
        var setting = $("#debug-getGroup").val();
        if (!setting) {
            return super.getGroup(environment, groupId);
        }
        else if (setting === "error") {
            await this._simulateError();
        }
        else {
            return Promise.resolve({
                "id": `${environment}.test-system.${groupId}`,
                "name": `Name of ${groupId}`,
                "description": `Description for ${groupId}`,
                "system": {
                    "id": "test-system",
                    "name": "Test policy"
                },
                "access": {
                    "membershipActive": false,
                    "satisfiedConstraints": [],
                    "unsatisfiedConstraints": [
                        {
                            "name": "__expiry",
                            "description": "You must choose an expiry between 1 minute and 1 day"
                        },
                        {
                            "name": "test",
                            "description": "Another unsatisfied constraint"
                        }
                    ],
                    "input": [
                        {
                            "name": "__expiry",
                            "description": "Expiry",
                            "type": "Duration",
                            "value": null,
                            "minInclusive": "60",
                            "maxInclusive": "86400"
                        },
                        {
                            "name": "justification",
                            "description": "Justification",
                            "type": "String",
                            "value": null
                        }
                    ]
                }
            });
        }
    }

}