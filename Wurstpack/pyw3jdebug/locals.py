from PythonCard import model

colhead = ['Name','Type','Value']

class Locals(model.Background):
	def on_initialize(self,event):
		self.locals = {}
		self.components.list.columnHeadings = colhead
	def update(self,newlocals):
		self.components.list.Clear()
		self.components.list.columnHeadings = colhead

		[self.components.list.Append([l]) for l in newlocals]

