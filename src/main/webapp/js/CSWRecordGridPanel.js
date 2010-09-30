/**
 * An extension of a normal GridPanel that makes it specialize into rendering a CSWRecordStore
 * 
 * This panel will create a 'copy' of the cswRecordStore with cswRecordFilter applied over the top of it.
 * Further - every time the cswRecordStore is altered, the 'copied' data will also be updated
 * 
 * id				: unique ID to identify this grid
 * title 			: The title this grid panel will display
 * cswRecordStore 	: an instance of CSWRecordStore that will be used to populate this panel
 * addLayerHandler	: function(CSWRecord) This will be called when the user adds a layer. 
 * cswRecordFilter	: function(CSWRecord) This will be called at load and whenever the underlying datastore 
 *                    changes. It will be used to filter the underlying datastore
 * visibleFilterHandler : function(record) This will be called on each record to test if they are visible on the map
 * showBoundsHandler: function(CSWRecord) called when the user wants to see a brief highlight of the records bounds
 * moveToBoundsHandler: function(CSWRecord) called when the user wants to find the location of the record bounds                                              
 *                    
 */
CSWRecordGridPanel = function(id, title, cswRecordStore, addLayerHandler, cswRecordFilter, visibleFilterHandler, showBoundsHandler, moveToBoundsHandler) {
	this.addLayerHandler = addLayerHandler;
	this.cswRecordFilter = cswRecordFilter;
	
	//Create our filtered datastore copy
	var dsCopy = new CSWRecordStore();
	cswRecordStore.on('datachanged', this.internalOnDataChanged, this);
	dsCopy.copyFrom(cswRecordStore, cswRecordFilter);
	
	//This is so we can reference our search panel
	var searchPanelId = id + '-search-panel';
	
	var rowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{dataIdentificationAbstract}</p><br>')
    });
	
	/*
	 * knownFeaturesPanel.on("cellclick", showRecordBoundingBox, knownFeaturesPanel.on);
    
    knownFeaturesPanel.on("celldblclick", moveToBoundingBox, knownFeaturesPanel);
	 * */
	
	CSWRecordGridPanel.superclass.constructor.call(this, {
		id				 : id, 
        stripeRows       : true,
        autoExpandColumn : 'title',
        plugins          : [ rowExpander ],
        viewConfig       : {scrollOffset: 0, forceFit:true},
        title            : title,
        region           :'north',
        split            : true,
        height           : 160,
        autoScroll       : true,
        store            : dsCopy,
        columns: [
            rowExpander,
            {
                id:'title',
                header: "Title",
                sortable: true,
                dataIndex: 'serviceName'
            }, {
            	id:'search',
            	header: '',
            	width: 45,
            	dataIndex: 'geographicElements',
            	resizable: false,
            	menuDisabled: true,
            	sortable: false,
            	fixed: true,
            	renderer: function (value) {
            		if (value.length > 0) {
            			return '<img src="img/magglass.gif"/>';
            		} else {
            			return '';
            		}
            	}
            },{
                id:'contactOrg',
                header: "Provider",
                width: 160,
                sortable: true,
                dataIndex: 'contactOrganisation',
                hidden:true
            }
        ],
        bbar: [{
            text:'Add Layer to Map',
            tooltip:'Add Layer to Map',
            iconCls:'add',
            pressed:true,
            scope:this,
            handler: function() {
        		var cswRecordToAdd = new CSWRecord(this.getSelectionModel().getSelected());
        		addLayerHandler(cswRecordToAdd);
        	}
        }],
        
        view: new Ext.grid.GroupingView({
            forceFit:true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
        }),
        tbar: [
               'Search: ', ' ',
               new Ext.ux.form.ClientSearchField({
                   store: dsCopy,
                   width:200,
                   id: searchPanelId,
                   fieldName:'serviceName'
               }), {
            	   	xtype:'button',
            	   	text:'Visible',
            	   	handler:function() {
            	   		var searchPanel = Ext.getCmp(searchPanelId);
            	   		searchPanel.runCustomFilter('<visible layers>', visibleFilterHandler);
               		}
               }
           ],
        listeners: {
        	cellclick : function (grid, rowIndex, colIndex, e) {
            	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
            	if (fieldName !== 'geographicElements') {
            		return;
            	}
            	
            	e.stopEvent();
            	
            	showBoundsHandler(grid.getStore().getCSWRecordAt(rowIndex));
        	},
        	
        	celldblclick : function (grid, rowIndex, colIndex, e) {
            	var record = grid.getStore().getAt(rowIndex);
            	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
            	if (fieldName !== 'geographicElements') {
            		return;
            	}
            	
            	e.stopEvent();
            	
            	moveToBoundsHandler(grid.getStore().getCSWRecordAt(rowIndex));
        	}
        }

    });
};

CSWRecordGridPanel.prototype.addLayerHandler = null;
CSWRecordGridPanel.prototype.cswRecordFilter = null;


Ext.extend(CSWRecordGridPanel, Ext.grid.GridPanel, {
	/**
	 * Whenever the internal datastore changes, update our filtered copy
	 * @param store
	 * @return
	 */
	internalOnDataChanged	: function(store) {
		this.getStore().copyFrom(store, this.cswRecordFilter);
	}
});