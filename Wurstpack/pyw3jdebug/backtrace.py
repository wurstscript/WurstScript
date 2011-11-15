#!/usr/bin/python

from PythonCard import model

colhead = ['Function']

class Backtrace(model.Background):
	def on_initialize(self, event):
		self.components.list.columnHeadings = colhead
	def update(self,newbt):
		self.components.list.Clear()
		self.components.list.columnHeadings = colhead
		[self.components.list.Append([l]) for l in newbt]

