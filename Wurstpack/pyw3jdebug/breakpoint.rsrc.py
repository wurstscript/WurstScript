{'application':{'type':'Application',
          'name':'Template',
    'backgrounds': [
    {'type':'Background',
          'name':'bgTemplate',
          'title':u'Breakpoints',
          'size':(374, 476),
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

{'type':'Button', 
    'name':'delbp', 
    'position':(195, 405), 
    'size':(80, -1), 
    'label':u'Del Func BP', 
    },

{'type':'TextField', 
    'name':'delbptxt', 
    'position':(15, 405), 
    'size':(175, -1), 
    },

{'type':'Button', 
    'name':'addbp', 
    'position':(195, 380), 
    'size':(81, -1), 
    'label':u'Add Func BP', 
    },

{'type':'TextField', 
    'name':'addbptxt', 
    'position':(15, 380), 
    'size':(175, -1), 
    },

{'type':'MultiColumnList', 
    'name':'list', 
    'position':(5, 5), 
    'size':(356, 371), 
    'backgroundColor':(255, 255, 255, 255), 
    'columnHeadings':[], 
    'font':{'faceName': u'Tahoma', 'family': 'sansSerif', 'size': 8}, 
    'items':[], 
    'maxColumns':20, 
    'rules':1, 
    },

] # end components
} # end background
] # end backgrounds
} }
