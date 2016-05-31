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
		/* Show the list of the rule name */
		{
			xtype: 'treepanel',
			width: 200,
			title: 'Rule List',
			/* Get tree type rule list from the server */
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
			/* When click the item in the list, fill the form with the rule information */
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
								/* Make enough type field */
								for (i = 0; i < resultjson.typenum; i++) {
									/* Make the container */
									var typescreen = Ext.create('Ext.form.FieldSet',
									{
										xtype: 'fieldcontainer',
										id: Ext.String.format('controllertypescreen' + i),
										layout: 'hbox',
										items: [{
										}]
									});
									/* Make the text field */
									var typetext = Ext.create('Ext.form.field.Text',
									{
										fieldLabel: 'Type Name',
										id: Ext.String.format('controllertypename' + i),
										allowBlank: false
									});
									/* Make the number field */
									var typenumtext = Ext.create('Ext.form.field.Number',
									{
										fieldLabel: 'Number',
										id: Ext.String.format('controllertypenum' + i),
										minValue: 1,
										value: 1,
										allowBlank: false
									});
									/* Add text field and number field into container */
									typescreen.add(typetext);
									typescreen.add(typenumtext);
									view.up('form').down('fieldset').add(typescreen);
								}
								/* Make enough attribute field */
								for (i = 0; i < resultjson.attnum; i++) {
									/* Make the container */
									var attributescreen = Ext.create('Ext.Panel',
									{
										layout: 'hbox',
										id: Ext.String.format('controllerpanel' + i),
										items: [{
										}]
									});
									/* make the text field */
									var attributetext = Ext.create('Ext.form.field.Text',
									{
										fieldLabel: Ext.String.format('Attribute Name'),
										id: Ext.String.format('controllerattribute' + i),
										allowBlank: false
									});
									/* Add textfield into the container */
									attributescreen.add(attributetext);
									view.up('form').down('fieldcontainer').add(attributescreen);
								}
								/* Fill the Rule information form */
								view.up('form').getForm().load({
									url: 'api/getrule/' + rec.get('text'),
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
		/* This container shows the rule information */
		{
			margin: '0 0 0 10',
			xtype: 'fieldcontainer',
			title:'Type details',
			reference: 'controllertypelist',
			layout: 'anchor',
			defaultType: 'textfield',
			items: [
				/* Rule unique id */
				{
					xtype: 'textfield',
					fieldLabel: 'Rule ID',
					id: 'controlleruniqueid',
					allowBlank: false
				},
				/* Rule Name */
				{
					xtype: 'textfield',
					fieldLabel: 'Rule Name',
					id: 'controllername',
					allowBlank: false
				},
				/* Show the list of type. Field start with controllertypescreen0, include controllertypename0, controllertypenum0 */
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
				/* Show the rule duration */
				{
					xtype: 'numberfield',
					fieldLabel: 'Duration (ms)',
					id: 'controllerduration',
					minValue: 0,
					value: 0,
					allowBlank: false
				},
				/* Show the rule ordered */
				{
					xtype: 'radiogroup',
					fieldLabel: 'Ordered',
					columns: 3,
					defaults: {
						name: 'controllerordered' //Each radio has the same name so the browser will make sure only one is checked at once
					},
					items: [
						/* When ordered true */
						{
							inputValue: 'true',
						   	boxLabel: 'true',
						   	id: 'controllertrue'
					   	},
						/* When ordered false, initial value is false */
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
		/* Reload rule treepanel */
		{
			xtype: 'button',
			text: 'Refresh',
			handler: function() {
				var me = this;
				me.up('form').down('treepanel').getStore().load();
			}
		},
		/* Delete specific rule */
		{
			xtype: 'button',
			text: 'Delete This Rule',
			handler: function() {
				var me = this;
				/* Request server with rule's unique id */
				Ext.Ajax.request({
					url:"api/deleterule/" + Ext.String.format(Ext.getCmp('controlleruniqueid').getValue()),
					method:"GET",

					success:function(result, request) {
						/* Success to delete rule */
						Ext.Msg.alert("Success", "Success to delete this rule.");
					},
					failure:function(result, request) {
						/* Fail to connect */
						Ext.Msg.alert("Failed");
					}
				});
				/* Reload rule treepanel */
				me.up('form').down('treepanel').getStore().load();
			}
		},
		/* Delete all rule */
		{
			xtype: 'button',
			text: 'Delete All Rule',
			handler: function() {
				var me = this;
				/* Request server */
				Ext.Ajax.request({
					url:"api/deleteallrule",
					method:"GET",

					success:function(result, request) {
						/* Success to delete rule */
						Ext.Msg.alert("Success", "Success to delete all rule.");
					},
					failure:function(result, request) {
						/* Fail to connect */
						Ext.Msg.alert("Failed");
					}
				});
				/* Reload rule treepanel */
				me.up('form').down('treepanel').getStore().load();
			}
		}]
});
