/*
 * This view can be shown when user entered http://localhost:8080/logax/#rulelist
 * Or click the Rule List on the left tab
 */

Ext.define('logax.view.rulelist.RuleList', {
	extend: 'Ext.container.Container',
	xtype: 'rulelist',
	cls: 'userProfile-container',

	requires: [
		'Ext.ux.layout.ResponsiveColumn'
	],

    layout: 'responsivecolumn',

	items: [
		{		
			xtype: 'rulelistform',
            userCls: 'big-100 small-100 shadow'
	    }
    ]
});
