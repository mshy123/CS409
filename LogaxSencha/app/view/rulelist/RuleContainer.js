Ext.define('logax.view.rulelist.RuleContainer', {
	extend: 'Ext.form.Panel',
	xtype: 'rulelistform',

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
	title: 'Rule Controller',
	bodyPadding: 5,
	layout: 'column',
	items: [
		{
			xtype: 'treepanel',
			width: 200,
			title: 'Rule List',
			store: Ext.create('Ext.data.TreeStore', {
				autoLoad: true,
				
				proxy :{
					type: 'ajax',
	  			  	url: 'api/ruletreelist',
	   		 		reader: {
						type: 'json'
		  		 	}
				},
				root: {
					expanded: true,
					text: "Rule List"
				}
			}),
			listeners: {
				itemclick : function(view, rec, item, index, eventObj) {
					if (rec.get('leaf')) {
						Ext.Ajax.request({
							url:"api/getruleframe/" + rec.get('text'),
							method:"GET",

							success:function(result, request) {
								var i;
								view.up('form').down('fieldset').removeAll();
								for (i = 0; i < logax.store.RuleNumber.attnum; i++) {
									view.up('form').down('fieldcontainer').remove(Ext.String.format('controllerpanel' + i));
								}
								var resultjson = Ext.JSON.decode(result.responseText);
								logax.store.RuleNumber.attnum = resultjson.attnum;
								for (i = 0; i < resultjson.typenum; i++) {
									var typescreen = Ext.create('Ext.form.FieldSet',
									{
										xtype: 'fieldcontainer',
										id: Ext.String.format('controllertypescreen' + i),
										layout: 'hbox',
										items: [{
										}]
									});
									var typetext = Ext.create('Ext.form.field.Text',
									{
										fieldLabel: 'Type Name',
										id: Ext.String.format('controllertypename' + i),
										allowBlank: false
									});
									var typenumtext = Ext.create('Ext.form.field.Number',
									{
										fieldLabel: 'Number',
										id: Ext.String.format('controllertypenum' + i),
										minValue: 1,
										value: 1,
										allowBlank: false
									});
									typescreen.add(typetext);
									typescreen.add(typenumtext);
									view.up('form').down('fieldset').add(typescreen);
								}
								for (i = 0; i < resultjson.attnum; i++) {
									var attributescreen = Ext.create('Ext.Panel',
									{
										layout: 'hbox',
										id: Ext.String.format('controllerpanel' + i),
										items: [{
										}]
									});
									var attributetext = Ext.create('Ext.form.field.Text',
									{
										fieldLabel: Ext.String.format('Attribute Name'),
										id: Ext.String.format('controllerattribute' + i),
										allowBlank: false
									});
									attributescreen.add(attributetext);
									view.up('form').down('fieldcontainer').add(attributescreen);
								}
								view.up('form').getForm().load({
									url: 'api/getrule/' + rec.get('text'),
									params: rec.get('text'),
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
			reference: 'controllertypelist',
			layout: 'anchor',
			defaultType: 'textfield',
			items: [
				{
					xtype: 'textfield',
					fieldLabel: 'Rule ID',
					id: 'controlleruniqueid',
					allowBlank: false
				},
				{
					xtype: 'textfield',
					fieldLabel: 'Rule Name',
					id: 'controllername',
					allowBlank: false
				},
				{
					xtype: 'fieldset',
					title: 'Type List',
					collapsible: true,
					layout: 'vbox',
					defaults: {
						flex: 1
					},
					items: [{
					}]
				},
				{
					xtype: 'numberfield',
					fieldLabel: 'Duration',
					id: 'controllerduration',
					minValue: 0,
					value: 0,
					allowBlank: false
				},
				{
					xtype: 'radiogroup',
					fieldLabel: 'Ordered',
					columns: 3,
					defaults: {
						name: 'controllerordered' //Each radio has the same name so the browser will make sure only one is checked at once
					},
					items: [
						{
							inputValue: 'true',
						   	boxLabel: 'true',
						   	id: 'controllertrue'
					   	},
						{
							inputValue: 'false',
						  	boxLabel: 'false',
						   	id: 'controllerfalse',
							checked: true
					   	}
					]
				}
			]
		}
	],
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
			text: 'Delete This Rule',
			handler: function() {
				var me = this;
				Ext.Ajax.request({
					url:"api/deleterule/" + Ext.String.format(Ext.getCmp('controlleruniqueid').getValue()),
					method:"GET",

					success:function(result, request) {
						Ext.Msg.alert("Success", "Success to delete this rule.");
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
			text: 'Delete All Rule',
			handler: function() {
				var me = this;
				Ext.Ajax.request({
					url:"api/deleteallrule",
					method:"GET",

					success:function(result, request) {
						Ext.Msg.alert("Success", "Success to delete all rule.");
					},
					failure:function(result, request) {
						Ext.Msg.alert("Failed");
					}
				});
				me.up('form').down('treepanel').getStore().load();
			}
		}]
});
