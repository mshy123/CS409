/*
 * This view will be shown when user enter the http://localhost:8080/logax/#rule
 * Or click the rule on the left tab
 */

Ext.define('logax.view.rule.Rule', {
	extend: 'Ext.container.Container',
	xtype: 'rule',
	cls: 'userProfile-container',

	requires: [
		'Ext.ux.layout.ResponsiveColumn'
	],

    layout: 'responsivecolumn',

	items: [
		{		
			xtype: 'addruleform',
            userCls: 'big-100 small-100 shadow'
	    }
    ]
});
