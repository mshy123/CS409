Ext.define('logax.view.dashboard.Type', {
	extend: 'Ext.panel.Panel',
	xtype: 'dashboardform',
	
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
				xtype: 'textfield',
				id: 'priority',
				fieldLabel: 'Priority',
				allowBlank: false
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
	
