{'application':{'type':'Application',
          'name':'Template',
    'backgrounds': [
    {'type':'Background',
          'name':'Background',
          'title':u'pyW3JDebug',
          'size':(408, 507),
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

{'type':'TextField', 
    'name':'funcname', 
    'position':(70, 20), 
    'size':(200, -1), 
    },

{'type':'StaticText', 
    'name':'StaticText1', 
    'position':(20, 25), 
    'text':u'Function', 
    },

{'type':'Button', 
    'name':'run', 
    'position':(100, 430), 
    'label':u'Run', 
    },

{'type':'Button', 
    'name':'step', 
    'position':(20, 430), 
    'label':u'Step', 
    },

{'type':'MultiColumnList', 
    'name':'disas', 
    'position':(10, 60), 
    'size':(381, 365), 
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
