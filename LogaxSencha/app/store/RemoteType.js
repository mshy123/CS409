Ext.define('logax.store.RemoteType', {
	extend: 'Ext.data.JsonStore',
	
	alias: 'store.remotetype',
	storeId: 'remotetype',

	autoLoad: true,
	proxy :{
		type: 'ajax',
		url: 'api/getjsontypelist',
		reader: { 
			type: 'json',
			root: 'types'
		}
	},
	fields: [
		{type : 'integer', name : 'code'},
		{type : 'string', name : 'typename'}
	]   
})
