Ext.define('logax.view.rule.AddRule', {
	extend: 'Ext.form.Panel',
	xtype: 'addruleform',


	requires: [
		'Ext.form.Panel',
		'Ext.form.FieldSet',
		'Ext.layout.container.Column',
		'Ext.layout.container.Anchor',
		'Ext.form.*',
		'Ext.form.field.ComboBox',
		'Ext.tree.Panel',
		'Ext.data.TreeStore',
		'Ext.data.JsonStore'
	],
	
	frame: true,
	title: 'Rule Maker',
	bodyPadding: 5,
	layout: 'column',
	items: [
		{
			margin: '0 0 0 10',
			xtype: 'fieldcontainer',
			title:'Type details',
			reference: 'typelist',
			layout: 'anchor',
			defaultType: 'textfield',
			items: [
				/* Rule name text field */
				{
					xtype: 'textfield',
					fieldLabel: 'Rule Name',
					id: 'name',
					allowBlank: false
				},
				/* Rule Type field */
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
				/* Rule duration text field */
				{
					xtype: 'numberfield',
					fieldLabel: 'Duration (ms)',
					id: 'duration',
					minValue: 0,
					value: 0,
					allowBlank: false
				},
				/* Rule ordered field */
				{
					xtype: 'radiogroup',
					fieldLabel: 'Ordered',
					columns: 3,
					defaults: {
						name: 'ordered' //Each radio has the same name so the browser will make sure only one is checked at once
					},
					items: [
						{
							inputValue: 'true',
						   	boxLabel: 'true',
						   	id: 'true'
					   	},
						{
							inputValue: 'false',
						  	boxLabel: 'false',
						   	id: 'false',
							checked: true
					   	}
					]
				}
			]
		}
	],
	tools: [
		/* Add new type container in the rule type field */
		{
			xtype: 'button',
			text: 'Add Type',
			handler: function() {
				var me = this;
				var num = logax.store.TypeNumber.typenum;
				logax.store.TypeNumber.typenum = num + 1;
				logax.store.TypeNumber.typelist.push(num);
				/* Show list of the type. User can click to add it */
				var typetext2 = Ext.create('Ext.form.field.ComboBox',
				{
					fieldLabel: 'Type List',
					triggerAction: 'all',
					name: 'typename',
					id: Ext.String.format('typelistfield' + num),
					store: Ext.create('Ext.data.JsonStore', {
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
	    		    }),
					displayField: 'typename',
					valueField:'code',
					typeAhead: true,
					forceSelection: true,
					selectOnFocus: true,
					queryMode: 'local'
				});
				var typescreen = Ext.create('Ext.form.FieldSet',
				{
					xtype: 'fieldcontainer',
					name: Ext.String.format('typescreen' + num),
					layout: 'hbox',
					items: [{
					}]
				});
				/* Number of the type */
				var typenumtext = Ext.create('Ext.form.field.Number',
				{
					fieldLabel: 'Number',
					id: Ext.String.format('typenum' + num),
					minValue: 1,
					value: 1,
					allowBlank: false
				});
				/* Delete this container */
				var deletetypescreen = Ext.create('Ext.Button',
				{
					text: '-',
					handler: function() {
						var i;
						var typelistnum = logax.store.TypeNumber.typelist;
						logax.store.TypeNumber.typelist = [];
						for (i = 0; i < typelistnum.length; i++) {
							if (typelistnum[i] != num) {
								logax.store.TypeNumber.typelist.push(typelistnum[i]);
							}
						}
						me.up('form').down('fieldset').remove(typescreen, true);
					}
				});
				typescreen.add(typetext2);
				typescreen.add(typenumtext);
				typescreen.add(deletetypescreen);
				me.up('form').down('fieldset').add(typescreen);
			}
		},
		/* When user click this button, Add attribute field in the form */
		{
			xtype: 'button',
			text: 'Add Attribute',
			handler: function() {
				var me = this;
				var num = logax.store.TypeNumber.attnum;
				logax.store.TypeNumber.attnum = num + 1;
				logax.store.TypeNumber.attlist.push(num);
				var attributescreen = Ext.create('Ext.Panel',
				{
					layout: 'hbox',
					items: [{
					}]
				});
				var attributetext = Ext.create('Ext.form.field.Text',
				{
					fieldLabel: Ext.String.format('Attribute Name'),
					id: Ext.String.format('attribute' + num),
					allowBlank: false
				});
				/* Delete the attribute field */
				var deleteatt = Ext.create('Ext.Button',
				{
					text: '-',
					handler: function() {
						var i;
						var attlists = logax.store.TypeNumber.attlist;
						logax.store.TypeNumber.attlist = [];
						for (i = 0; i < attlists.length; i++) {
							if (attlists[i] != num) {
								logax.store.TypeNumber.attlist.push(attlists[i]);
							}
						}
						me.up('form').down('fieldcontainer').remove(attributescreen, true);
					}
				});
				attributescreen.add(attributetext);
				attributescreen.add(deleteatt);
				me.up('form').down('fieldcontainer').add(attributescreen);
			}
		},
		/* Reload all the type field. Type field can have lastest type list */
		{
			xtype: 'button',
			text: 'Refresh Type Field',
			handler: function() {
				var typelistnum = logax.store.TypeNumber.typelist;
				for (i = 0; i < typelistnum.length; i++) {
					Ext.getCmp(Ext.String.format('typelistfield' + typelistnum[i])).getStore().load();
				}
			}
		},
		/* Add Rule to DB. Send request /api/addrule with jsonrequest */
		{
			xtype: 'button',
			text: 'Commit',
			formBind: true,
			disabled: true,
			handler: function() {
				var me = this;
				var i;
				var typelistnum = logax.store.TypeNumber.typelist;
				var typejsonlist = [];
				var typejson;
				var attjsonlist = [];
				var attjson;
				/* Get type list */
				for (i = 0; i < typelistnum.length; i++) {
					typejson =
					{
						"name":Ext.getCmp(Ext.String.format('typelistfield' + typelistnum[i])).getRawValue(),
						"number":Ext.String.format(Ext.getCmp(Ext.String.format('typenum' + typelistnum[i])).getValue())
					};
					typejsonlist.push(typejson);
				}
				/* Get attribute list */
				var attlistnum = logax.store.TypeNumber.attlist;
				for (i = 0; i < attlistnum.length; i++) {
					attjson =
					{
						"name":Ext.getCmp(Ext.String.format('attribute' + attlistnum[i])).getValue()
					};
					attjsonlist.push(attjson);
				}
				/* Make jsonRequest */
				var jsonRequest = 
				{
					"name":Ext.getCmp('name').getValue(),
					"duration":Ext.String.format(Ext.getCmp('duration').getValue()),
					"ordered":Ext.ComponentQuery.query('[name=ordered]')[0].getGroupValue(),
					"types":typejsonlist,
					"attributes":attjsonlist
				};
				
				/* Send request */
				Ext.Ajax.request({
					url:"api/addrule",
					method:"POST",
					jsonData: jsonRequest,
					success:function(result, request) {
						var job = Ext.JSON.decode(result.responseText);
						if (!job.success) {
							/* Fail to add */
							Ext.Msg.alert("Fail", job.message);
						}
						else {
							/* Success to add */
							Ext.Msg.alert("Success", "Add Rule Name " + Ext.getCmp('name').getValue());
						}
					},
					failure:function(result, request) {
						Ext.Msg.alert("Failed");
					}
				});
			}
		}]
});
