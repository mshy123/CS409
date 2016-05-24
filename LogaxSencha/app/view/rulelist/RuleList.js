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
