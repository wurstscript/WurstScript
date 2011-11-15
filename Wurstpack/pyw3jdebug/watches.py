#!/usr/bin/python

from PythonCard import model

colhead = ['Expression','Value']

class Watch(model.Background):
	def on_initialize(self, event):
		self.watch.columnHeadings = colhead
	def on_addwatch_mouseClick(self,event):
		expr = self.components.nwtxt.text
		self.components.watch.Append([expr,""])
		print expr


