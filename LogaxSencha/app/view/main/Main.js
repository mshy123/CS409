/**
 * This class is the main view for the application. It is specified in app.js as the
 * "mainView" property. That setting automatically applies the "viewport"
 * plugin causing this view to become the body element (i.e., the viewport).
 */
Ext.define('logax.view.main.Main', {
    extend: 'Ext.container.Viewport',
    xtype: 'app-main',

    requires: [
		'Ext.list.Tree'
    ],

    controller: 'main',
    viewModel: 'main',

	cls: 'sencha-dash-viewport',
	itemId: 'mainView',

	layout: {
		type: 'vbox',
		align: 'stretch'
	},

	listeners: {
		render: 'onMainViewRender'
	},

	items: [
		{
			xtype: 'toolbar',
			cls: 'sencha-dash-dash-headerbar shadow',
			height: 64,
			itemId: 'headerBar',
			items: [
				{
					xtype: 'component',
					reference: 'logaxLogo',
					cls: 'sencha-logo',
					html: '<div class="main-logo"><img src="resources/images/logax-logo.png">Logax</div>',
					width: 250
				}
			]
		},
		{
			xtype: 'maincontainerwrap',
			id: 'main-view-detail-wrap',
			reference: 'mainContainerWrap',
			flex: 1,
			items: [
				/* This shows left tab. It has three tab, type, rule, rule controller */
				{
					xtype: 'treelist',
					reference: 'navigationTreeList',
					itemId: 'navigationTreeList',
					ui: 'navigation',
					store: 'NavigationTree',
					width: 250,
					expanderFirst: false,
					expanderOnly: false,
					listeners: {
						selectionchange: 'onNavigationTreeSelectionChange'
					}
				},
				{
					xtype: 'container',
					flex: 1,
					reference: 'mainCardPanel',
					cls: 'sencha-dash-right-main-container',
					itemId: 'contentPanel',
					layout: {
						type: 'card',
						anchor: '100%'
					}
				}
			]
		}
	]
});
