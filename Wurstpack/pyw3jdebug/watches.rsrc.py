{'application':{'type':'Application',
          'name':'Template',
    'backgrounds': [
    {'type':'Background',
          'name':'bgTemplate',
          'title':u'Watches',
          'size':(388, 442),
          'style':['resizeable'],

        'menubar': {'type':'MenuBar',
         'menus': [
             {'type':'Menu',
             'name':'menuFile',
             'label':'&File',
             'items': [
                  {'type':'MenuItem',
                   'name':'menuFileExit',
                   'label':'E&xit',
                   'command':'exit',
                  },
              ]
             },
         ]
     },
         'components': [

{'type':'MultiColumnList', 
    'name':'watch', 
    'position':(5, 5), 
    'size':(370, 355), 
    'backgroundColor':(255, 255, 255, 255), 
    'columnHeadings':[], 
    'font':{'faceName': u'Tahoma', 'family': 'sansSerif', 'size': 8}, 
    'items':[], 
    'maxColumns':20, 
    'rules':1, 
    },

{'type':'Button', 
    'name':'addwatch', 
    'position':(300, 365), 
    'label':u'Add Watch', 
    },

{'type':'TextField', 
    'name':'nwtxt', 
    'position':(5, 365), 
    'size':(290, -1), 
    },

] # end components
} # end background
] # end backgrounds
} }
