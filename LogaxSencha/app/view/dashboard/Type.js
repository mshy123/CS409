Ext.define('logax.view.dashboard.Type', {
	extend: 'Ext.form.Panel',
	xtype: 'dashboardform',

	requires: [
		'Ext.form.Panel',
		'Ext.form.FieldSet',
		'Ext.layout.container.Column',
		'Ext.layout.container.Anchor',
		'Ext.form.*',
		'Ext.tree.Panel',
		'Ext.data.TreeStore'
	],
	
	frame: true,
	title: 'Type Controller',
	bodyPadding: 5,
	height: 500,
	layout: 'column',
	items: [
		{
			xtype: 'treepanel',
			width: 200,
			title: 'Type List',
			store: Ext.create('Ext.data.TreeStore', {
				autoLoad: true,
				
				proxy :{
					type: 'ajax',
	  			  	url: 'api/typelist',
	   		 		reader: {
						type: 'json'
		  		 	}
				},
				root: {
					expanded: true,
					text: "Type List"
				}
			}),
			listeners: {
				itemclick : function(view, rec, item, index, eventObj) {
					if (rec.get('leaf')) {
						view.up('form').getForm().load({
							url: 'api/gettype/' + rec.get('text'),
							params: rec.get('text'),
							method: 'GET',
							waitMsg: 'Loading data...'
						});
					}
				}
			}
		},
		{
			margin: '0 0 0 10',
			xtype: 'fieldset',
			title:'Type details',
			layout: 'anchor',
			defaultType: 'textfield',
			items: [
				{
					fieldLabel: 'Type Name',
					id: 'typename'
				},
				{
					fieldLabel: 'Type Regex',
					id: 'typeregex'
				},
				{
					fieldLabel: 'Path',
					id: 'path'
				},
				{
					xtype: 'radiogroup',
					fieldLabel: 'Priority',
					columns: 3,
					defaults: {
						name: 'priority' //Each radio has the same name so the browser will make sure only one is checked at once
					},
					items: [{
						   inputValue: 'high',
						   boxLabel: 'high',
						   id: 'high'
					   }, {
						   inputValue: 'low',
						   boxLabel: 'low',
						   id: 'low'
					}]
				},
				{
					xtype: 'button',
					text: 'Edit',
					handler: function() {
						var me = this;
						var jsonRequest = 
						{
							"typename":Ext.getCmp('typename').getValue(),
							"typeregex":Ext.getCmp('typeregex').getValue(),
							"priority":Ext.ComponentQuery.query('[name=priority]')[0].getGroupValue(),
							"path":Ext.getCmp('path').getValue()
						};
						Ext.Ajax.request({
							url:"api/edittype",
							method:"POST",
							jsonData: jsonRequest,

							success:function(result, request){
								Ext.Msg.alert("Success", "Edit Type Name " + Ext.getCmp('typename').getValue());
							},
							failure:function(result, request){
								Ext.Msg.alert("Failed");
							}
						});

					}
				},
				{
					xtype: 'button',
					text: 'Delete',
					handler: function() {
						var me = this;
						var jsonRequest = 
						{
							"typename":Ext.getCmp('typename').getValue(),
							"typeregex":Ext.getCmp('typeregex').getValue(),
							"priority":Ext.ComponentQuery.query('[name=priority]')[0].getGroupValue(),
							"path":Ext.getCmp('path').getValue()
						};
						Ext.Ajax.request({
							url:"api/delete",
							method:"POST",
							jsonData: jsonRequest,

							success:function(result, request){
								var job = Ext.JSON.decode(result.responseText);
								if (!job.success) {
									Ext.Msg.alert("Fail", "This type is bind with Rule");
								}
								else {
									Ext.Msg.alert("Success", "Delete Type Name " + Ext.getCmp('typename').getValue());
								}
							},
							failure:function(result, request){
										Ext.Msg.alert("Failed");
									}
						});
						me.up('form').down('treepanel').getStore().load();
					}
				},
				{
					xtype: 'button',
					text: 'Add',
					handler: function() {
						var me = this;
						var jsonRequest = 
						{
							"typename":Ext.getCmp('typename').getValue(),
							"typeregex":Ext.getCmp('typeregex').getValue(),
							"priority":Ext.ComponentQuery.query('[name=priority]')[0].getGroupValue(),
							"path":Ext.getCmp('path').getValue()
						};
						Ext.Ajax.request({
							url:"api/execute",
							method:"POST",
							jsonData: jsonRequest,

							success:function(result, request){
								var job = Ext.JSON.decode(result.responseText);
								if (!job.success) {
									Ext.Msg.alert("Fail", "Already Exist " + Ext.getCmp('typename').getValue());
								}
								else {
									Ext.Msg.alert("Success", "Add Type Name " + Ext.getCmp('typename').getValue());
								}

							},
							failure:function(result, request){
								Ext.Msg.alert("Failed");
							}
						});
						me.up('form').down('treepanel').getStore().load();
					}
				}
			]
		}],
	tools: [
		{
			xtype: 'button',
			text: 'Refresh',
			handler: function() {
				var me = this;
				me.up('form').down('treepanel').getStore().load();
			}
		},
		{
			xtype: 'button',
			text: 'Delete All Type',

			handler: function() {
				var me = this;
				Ext.Ajax.request({
					url:"api/deletealltype",
					method:"GET",

					success:function(result, request){
						var job = Ext.JSON.decode(result.responseText);
						if (!job.success) {
							Ext.Msg.alert("Fail", "Type is bind with Rule");
						}
						else {
							Ext.Msg.alert("Success", "Success to delete all type");
						}
					},
					failure:function(result, request){
						Ext.Msg.alert("Connection Failed");
					}
				});
				me.up('form').down('treepanel').getStore().load();
			}
		}]
});
