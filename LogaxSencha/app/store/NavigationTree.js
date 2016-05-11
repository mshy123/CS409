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
				text: 'Make Type',
				iconCls: 'x-fa fa-desktop',
				viewType: 'dashboard',
				routeId: 'dashboard',
				leaf: true
			},
			{
				text: 'Will be added..',
				iconCls: 'x-fa fa-user',
				viewType: 'profile',
				leaf: true
			}
		]
	}
});
