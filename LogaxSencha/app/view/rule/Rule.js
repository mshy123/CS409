Ext.define('logax.view.rule.Rule', {
	extend: 'Ext.container.Container',
	xtype: 'rule',
	cls: 'userProfile-container',

	requires: [
		'Ext.ux.layout.ResponsiveColumn'
	],

    layout: 'responsivecolumn',

	items: [
		//{
		//	xtype: 'typelistform',
		//	userCls: 'big-100 small-100 shadow'
		//},
		{		
			xtype: 'addruleform',
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
