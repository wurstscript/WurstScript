#!/usr/bin/python

from PythonCard import model

colhead = ['Breakpoints']

class Breakpoint(model.Background):
	def on_initialize(self,event):
		self.parent = self.getParent()
		self.bp = {}
	def on_addbp_mouseClick(self,event):
		bp = self.components.addbptxt.text
		self.parent.cq.put('addbp '+bp)
	def on_delbp_mouseClick(self,event):
		bp = self.components.delbptxt.text
		self.parent.cq.put('delbp '+bp)
	
	def update(self,bp):
		self.components.list.Clear()
		self.components.list.columnHeadings = colhead
		[self.components.list.Append([l]) for l in bp]


