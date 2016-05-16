Ext.define('logax.store.NavigationTree', {
	extend: 'Ext.data.TreeStore',

	storeId: 'NavigationTree',

	fields: [{
		name: 'text'
	}],

	root: {
		expanded: true,
		children: [
			{
				text: 'Type',
				iconCls: 'x-fa fa-desktop',
				viewType: 'dashboard',
				routeId: 'dashboard',
				leaf: true
			},
			{
				text: 'Type List',
				iconCls: 'x-fa fa-user',
				viewType: 'profile',
				leaf: true
			}/*,
			{
				text: 'Rule',
				iconCls: 'x-fa fa-user',
				viewType: 'ruleview',
				leaf: true
			},
			{
				text: 'Rule List',
				iconCls: 'x-fa fa-user',
				viewType: 'rulelist',
				leaf: true
			}*/
		]
	}
});
