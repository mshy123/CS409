/*
 * This view is main page of the Type page.
 * You can enter by http://localhost:8080/logax/#dashboard
 * This page contain dashboardform
 */

Ext.define('logax.view.dashboard.Dashboard', {
	extend: 'Ext.container.Container',
	xtype: 'dashboard',
	cls: 'userProfile-container',

	requires: [
		'Ext.ux.layout.ResponsiveColumn'
	],

    layout: 'responsivecolumn',

	items: [
		{		
			xtype: 'dashboardform',
            userCls: 'big-100 small-100 shadow'
	    }
    ]
});
