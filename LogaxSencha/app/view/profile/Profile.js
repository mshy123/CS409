Ext.define('logax.view.profile.Profile', {
	extend: 'Ext.container.Container',
	xtype: 'profile',

	requires: [
	],

	controller: 'dashboard',
	viewModel: {
		type: 'dashboard'
	},

	items: [
		{
			xtype: 'typename',
        	name: 'Type Name',
        	fieldLabel: 'Enter Type name',
        	allowBlank: false  // requires a non-empty value
	    },
		{
			xtype: 'typeregex',
			name: 'Type Regex',
			fieldLabel: 'Enter Type Reguler expresstion',
			allowBlank: false
		}
	]
});
