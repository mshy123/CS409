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
						view.up('form').down('fieldset').removeAll();
						Ext.Ajax.request({
							url: 'api/gettypeframe/' + rec.get('text'),
							method: 'GET',
							
							success:function(result, request) {
								var i;
								view.up('form').down('fieldset').removeAll();
								var resultjson = Ext.JSON.decode(result.responseText);
								for (i = 0; i < resultjson.regexnum; i++) {
									var regextext = Ext.create('Ext.form.field.Text',
									{
										fieldLabel: Ext.String.format('Regex ' + i),
										id: Ext.String.format('typeregex' + i),
										allowBlank: false
									});
									view.up('form').down('fieldset').add(regextext);
								}
								view.up('form').getForm().load({
									url: 'api/gettype/' + rec.get('text'),
									method: 'GET'
								});
							},
							failure:function(result, request) {
								Ext.Msg.alert("Failed");
							}
						});
					}
				}
			}
		},
		{
			margin: '0 0 0 10',
			xtype: 'fieldcontainer',
			title:'Type details',
			layout: 'anchor',
			defaultType: 'textfield',
			items: [
				{
					fieldLabel: 'Type Name',
					id: 'typename',
					allowBlank: false
				},
				{
					xtype: 'panel',
					layout: 'hbox',
					items: [
						{
							xtype: 'textfield',
							fieldLabel: 'Number of regex',
							id: 'regexnum',
							allowBlank: true
						},
						{
							xtype: 'button',
							text: 'make',
							handler: function(){
								var num = Ext.getCmp('regexnum').getValue();
								var i;
								var me = this;
								me.up('form').down('fieldset').removeAll();
								for (i = 0; i < num; i++) {
									var regextext = Ext.create('Ext.form.field.Text',
									{
										fieldLabel: Ext.String.format('Regex ' + i),
										id: Ext.String.format('typeregex' + i),
										allowBlank: false
									});
									me.up('form').down('fieldset').add(regextext);
								}
							}
						}
					]
				},
				{
					xtype: 'fieldset',
					title: 'Regex List',
					collapsible: true,
					layout: 'vbox',
					defaults: {
						flex: 1
					},
					items: [{
					}]
				},
				{
					fieldLabel: 'Path',
					id: 'path',
					allowBlank: false
				},
				{
					xtype: 'radiogroup',
					fieldLabel: 'Priority',
					columns: 3,
					defaults: {
						name: 'priority'
					},
					items: [{
						   inputValue: 'high',
						   boxLabel: 'high',
						   id: 'high',
						   checked: true
					   }, {
						   inputValue: 'low',
						   boxLabel: 'low',
						   id: 'low'
					}]
				},
				{
					xtype: 'button',
					text: 'Add',
					formBind: true,
					disabled: true,
					handler: function() {
						var me = this;
						var regexlist = [];
						var num = Ext.getCmp('regexnum').getValue();
						var i;
						for (i = 0; i < num; i++) {
							var jsonregex =
							{
								"typeregex":Ext.getCmp(Ext.String.format('typeregex' + i)).getValue()
							};
							regexlist.push(jsonregex);
						}
						var jsonRequest = 
						{
							"typename":Ext.getCmp('typename').getValue(),
							"regexnum":num,
							"typeregex":regexlist,
							"priority":Ext.ComponentQuery.query('[name=priority]')[0].getGroupValue(),
							"path":Ext.getCmp('path').getValue()
						};
						Ext.Ajax.request({
							url:"api/addtype",
							method:"POST",
							jsonData: jsonRequest,

							success:function(result, request){
								var job = Ext.JSON.decode(result.responseText);
								if (!job.success) {
									Ext.Msg.alert("Fail", job.message);
								}
								else {
									Ext.Msg.alert("Success", "Add Type Name " + Ext.getCmp('typename').getValue());
								}

							},
							failure:function(result, request) {
								Ext.Msg.alert("Failed");
							}
						});
						me.up('form').down('treepanel').getStore().load();
					}
				},
				{
					xtype: 'button',
					text: 'Edit',
					formBind: true,
					disabled: true,
					handler: function() {
						var regexlist = [];
						var num = Ext.getCmp('regexnum').getValue();
						var i;
						var me = this;
						for (i = 0; i < num; i++) {
							var jsonregex =
							{
								"typeregex":Ext.getCmp(Ext.String.format('typeregex' + i)).getValue()
							};
							regexlist.push(jsonregex);
						}
						var jsonRequest = 
						{
							"typename":Ext.getCmp('typename').getValue(),
							"regexnum":num,
							"typeregex":regexlist,
							"priority":Ext.ComponentQuery.query('[name=priority]')[0].getGroupValue(),
							"path":Ext.getCmp('path').getValue()
						};
						Ext.Ajax.request({
							url:"api/edittype",
							method:"POST",
							jsonData: jsonRequest,

							success:function(result, request){
								var job = Ext.JSON.decode(result.responseText);
								if (!job.success) {
									Ext.Msg.alert("Fail", job.message);
								}
								else {
									Ext.Msg.alert("Success", "Edit Type Name " + Ext.getCmp('typename').getValue());
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
					text: 'Delete',
					handler: function() {
						var me = this;
						Ext.Ajax.request({
							url: Ext.String.format("api/deletetype/" + Ext.getCmp('typename').getValue()),
							method:"GET",

							success:function(result, request) {
								var job = Ext.JSON.decode(result.responseText);
								if (!job.success) {
									Ext.Msg.alert("Fail", job.message);
								}
								else {
									Ext.Msg.alert("Success", "Delete Type Name " + Ext.getCmp('typename').getValue());
								}
							},
							failure:function(result, request) {
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
				me.up('form').getForm().reset();
				me.up('form').down('fieldset').removeAll();
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
							Ext.Msg.alert("Fail", job.message);
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
