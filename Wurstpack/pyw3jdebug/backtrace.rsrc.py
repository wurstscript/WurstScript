{'application':{'type':'Application',
          'name':'Template',
    'backgrounds': [
    {'type':'Background',
          'name':'bgTemplate',
          'title':'Backtrace',
          'size':(414, 419),
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
    'name':'list', 
    'position':(5, 5), 
    'size':(395, 362), 
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
