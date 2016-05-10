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
/*
	requires: [
		'Ext.container.Container'
	],

	layout: {
		type: 'vbox'
	},

	items: [
		{
			xtype: 'textfield',
			name: 'typename',
			id: 'typename',
			fieldLabel: 'Wow',
			allowBlank: false
		},
		{
			xtype: 'dashboardform'
		}
	]*/
});
