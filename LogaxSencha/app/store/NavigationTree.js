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
				text: 'Dashboard Change',
				iconCls: 'x-fa fa-desktop',
				viewType: 'dashboard',
				routeId: 'dashboard',
				leaf: true
			},
			{
				text: 'Profile',
				iconCls: 'x-fa fa-user',
				viewType: 'profile',
				leaf: true
			}
		]
	}
});
