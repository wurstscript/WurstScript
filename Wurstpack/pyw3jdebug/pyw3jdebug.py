#!/usr/bin/python

from PythonCard import model
from threading import *
from ctypes import *
from Queue import *
from time import *
import exceptions

import locals
import stack
import backtrace
import breakpoint

GENERIC_READ = 0x80000000
GENERIC_WRITE = 0x40000000
RWFLAG = c_int(GENERIC_READ|GENERIC_WRITE)
OPEN_EXISTING = c_int(3)
INVALID_HANDLE_VALUE = c_int(0xffffffff)
PIPE_READMODE_MESSAGE = c_int(2)

PIPENAME = '\\\\.\\pipe\\w3jdebugpipe'

timeout = 50

class ReadFileError(exceptions.Exception):
	def __init__(self):
		return
	def __str__(self):
		print "","Read file error occurred"

class PipeClient:
	def __init__(self,pipename):
		self.pipename = pipename
		self.connected = False
	def connect(self):
		self.connected = False
		while True:
			self.pipe = windll.kernel32.CreateFileA(self.pipename,
					RWFLAG,
					0,0,OPEN_EXISTING,0,0)
			if self.pipe != INVALID_HANDLE_VALUE: break
			if not windll.kernel32.WaitNamedPipe(self.pipename,timeout):
				print 'Could not open pipe'
				return False

		mode = PIPE_READMODE_MESSAGE
		pmode = pointer(mode)

		s = windll.kernel32.SetNamedPipeHandleState(self.pipe,pmode,0,0)
		if not s:
			print 'Debug server not detected'
			exit()
			return False

		self.connected = True
		return True
	def write(self,msg):
		if not self.connected: return False
		nwritten = c_int(0)
		amsg = str(msg)			# strip unicode
		print 'wrote: ',amsg
		r = windll.kernel32.WriteFile(self.pipe,amsg,len(amsg),pointer(nwritten),0)
		if r == 0:
			raise WriteFileError
	def read(self):
		if not self.connected: return False
		nread = c_int(0)
		size = 1024
		buf = create_string_buffer(size)
		str = ""

		done = False
		while not done:
			r = windll.kernel32.ReadFile(self.pipe,buf,size,pointer(nread),0)
			if r != 0:
				str+=buf.value
				break
			elif size == nread.value:
				str+=buf.value
			else:
				raise ReadFileError

		print 'read: ',str
		return str

class AddOnBreak:
	def __init__(self):
		self.canpush = False
	def put(self,cmd):			# try to put a command
		if self.canpush:
			self.cmd = cmd
			self.canpush = False
	def get(self):				# get a command, wait if there's none
		self.canpush = True
		while self.canpush: sleep(0.05)
		cmd = self.cmd
		return cmd


class Debug:
	def __init__(self,gui):
		self.wc3 = PipeClient(PIPENAME)
		self.wc3.connect()
		self.gui = gui
		self.cq = self.gui.cq
		self.oldfunc = ""
		self.curfunc = ""
		self.selected = -1
	def update(self):
		self.updatefuncname()
		self.updatelocals()
		self.updatestack()
		self.updatebt()
		self.updatebp()
		if self.oldfunc != self.curfunc or self.curfunc == "": self.updatedisas()
		if self.oldfunc == self.curfunc and self.selected >= 0:
			self.gui.components.disas.SetSelection(self.selected,0)
		self.updateopnum()
	def updatefuncname(self):
		self.wait()
		self.wc3.write('funcname')
		self.oldfunc = self.curfunc
		self.curfunc = self.wc3.read()
		self.gui.components.funcname.text = self.curfunc
	def updatelocals(self):
		self.wait()
		self.wc3.write('locals')
		lines = [line.split() for line in self.wc3.read().split('\n')]
		locals = [l for l in lines if len(l) == 3]
		self.gui.localsWindow.update(locals)
	def updatestack(self):
		self.wait()
		self.wc3.write('stack')
		lines = [line.split() for line in self.wc3.read().split('\n')]
		stack = [s for s in lines if len(s) == 2]
		self.gui.stackWindow.update(stack)
	def updatebt(self):
		self.wait()
		self.wc3.write('backtrace')
		#lines = [line.split() for line in self.wc3.read().split('\n')]
		#bt = lines #[s for s in lines if len(s) == 1]
		bt = [line for line in self.wc3.read().split('\n')]
		self.gui.btWindow.update(bt)
	def updatebp(self):
		self.wait()
		self.wc3.write('listbp')
		lines = [line.split() for line in self.wc3.read().split('\n')]
		bp = [s for s in lines if len(s) == 1]
		self.gui.bpWindow.update(bp)
	def updatedisas(self):
		self.wait()
		self.gui.components.disas.Clear()
		self.gui.components.disas.columnHeadings = ['i','r1','r2','r3','Op','Argument']
		self.wc3.write('disas')
		for line in self.wc3.read().split('\n'):
			l = line.split()
			if len(l) == 6:	self.gui.components.disas.Append([l])
	def updateopnum(self):
		self.wait()
		self.wc3.write('opnum')
		self.selected = int(self.wc3.read())
		self.gui.components.disas.SetSelection(self.selected)
	def wait(self):
		while True:
			if not self.wc3.connected: exit() 
			msg = self.wc3.read()
			print 'waiting for request for control'
			if msg == 'askforinput': break

class WorkerThread(Thread):
	def __init__(self,app):
		Thread.__init__(self)
		self.app = app
		self.dbg = Debug(app)
	def run(self):
		while True:
			self.dbg.update()
			self.dbg.wait()
			cmd = self.app.cq.get()
			self.dbg.wc3.write(cmd)

class GUI(model.Background):
	def on_initialize(self,event):
		self.cq = AddOnBreak()
		self.components.disas.columnHeadings = ['i','r1','r2','r3','Op','Argument']
		self.localsWindow = model.childWindow(self,locals.Locals)
		self.stackWindow = model.childWindow(self,stack.Stack)
		self.btWindow = model.childWindow(self,backtrace.Backtrace)
		self.bpWindow = model.childWindow(self,breakpoint.Breakpoint)
		self.thread = WorkerThread(self).start()
	def on_step_mouseClick(self,event):
		self.cq.put('step')
	def on_run_mouseClick(self,event):
		self.cq.put('run')



app = model.Application(GUI)
app.MainLoop()
