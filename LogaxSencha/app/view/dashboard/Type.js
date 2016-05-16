Ext.define('logax.view.dashboard.Type', {
	extend: 'Ext.panel.Panel',
	xtype: 'dashboardform',
	
	requires: [
		'Ext.form.*'
	],
	
bodyPadding: '10',
	layout: 'fit',
	
	items: [{
		xtype: 'container',
		items: [
			{
				xtype: 'textfield',
				id: 'typename',
				fieldLabel: 'Type Name',
				allowBlank: false
			},
			{
				xtype: 'textfield',
				id: 'typeregex',
				fieldLabel: 'Type Regular Expression',
				allowBlank: false
			},
			{
				xtype: 'textfield',
				id: 'path',
				fieldLabel: 'Path',
				allowBlank: false
			},
			{
				xtype: 'textfield',
				id: 'pos_file',
				fieldLabel: 'Pos_file',
				allowBlank: false
			},
			{
				xtype      : 'fieldcontainer',
				fieldLabel : 'Priority',
				defaultType: 'radiofield',
				defaults: {
					flex: 1
				},
				layout: 'hbox',
				items: [
					{
						boxLabel  : 'High',
						name      : 'priority',
						inputValue: 'high',
						id        : 'high'
					}, {
						boxLabel  : 'Low',
						name      : 'priority',
						inputValue: 'low',
						id        : 'low'
					}
				]
			},
			{
				xtype: 'button',
				text: 'Confirm',
				handler: function() {
					var me = this;
					var jsonResult;
					var jobId;
					var jsonStatus;
					var jobStatus = "running";
					var	jsonRequest =
						{
							"typename":Ext.getCmp('typename').getValue(),
							"typeregex":Ext.getCmp('typeregex').getValue(),
							"priority":Ext.ComponentQuery.query('[name=priority]')[0].getGroupValue(),
							"path":Ext.getCmp('path').getValue(),
							"pos_file":Ext.getCmp('pos_file').getValue()
						};
					Ext.Ajax.request({
						url:"api/execute",
						method:"POST",
						jsonData: jsonRequest,

						success:function(result, request){
							jobId = Ext.JSON.decode(result.responseText);
							me.jobID = jobId.jobid;
							Ext.Msg.alert("Success", "Add Type Name " + Ext.getCmp('typename').getValue());
							//me.onJobFinish();
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
					var jsonResult;
					var jobId;
					var jsonStatus;
					var jobStatus = "running";
					var jsonRequest = 
					{
						"typename":Ext.getCmp('typename').getValue(),
						"typeregex":Ext.getCmp('typeregex').getValue(),
						"priority":Ext.getCmp('priority').getValue(),
						"path":Ext.getCmp('path').getValue(),
						"pos_file":Ext.getCmp('pos_file').getValue()
					};
					Ext.Ajax.request({
						url:"api/delete",
						method:"POST",
						jsonData: jsonRequest,

						success:function(result, request){
							jobId = Ext.JSON.decode(result.responseText);
							me.jobID = jobId.jobid;
							Ext.Msg.alert("Success", "Delete Type Name " + Ext.getCmp('typename').getValue());
							//me.onJobFinish();
						},
						failure:function(result, request){
									Ext.Msg.alert("Failed");
								}
					});
				}
			}
		]
	}]/*,
	onButtonClick: function() {
		var me = this;
		var jsonResult;
		var jobId;
		var jsonStatus;
		var jobStatus = "running";
		var jsonRequest = 
			{
				"typeName":Ext.getCmp('typename').getValue(),
				"typeRegex":Ext.getCmp('typeregex').getValue(),
				"priority":Ext.getCmp('priority').getValue(),
				"path":Ext.getCmp('path').getValue(),
				"pos_file":Ext.getCmp('pos_file').getValue()
			};
		Ext.Ajax.request({
				url:"api/execute",
				method:"POST",
				jsonData: jsonRequest,

				success:function(result, request){
					jobId = Ext.JSON.decode(result.responseText);
					me.jobID = jobId.jobid;
					Ext.Msg.alert("Success", "Algorithm is running: " + me.jobID);
					//me.onJobFinish();
				},
				failure:function(result, request){
					Ext.Msg.alert("Failed");
				}
		});
	}*/
});
	
