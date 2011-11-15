#!/usr/bin/python

from PythonCard import model

colhead = ['Type','Value']

class Stack(model.Background):
	def on_initialize(self, event):
		self.components.list.columnHeadings = colhead
	def update(self,newstack):
		self.components.list.Clear()
		self.components.list.columnHeadings = colhead
		[self.components.list.Append([l]) for l in newstack]

