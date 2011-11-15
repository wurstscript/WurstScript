from distutils.core import setup

# py2exe stuff
import py2exe, os
# find pythoncard resources to add as 'data_files'
pycard_resources=[]
for filename in os.listdir('.'):
    if filename.find('.rsrc.')>-1:
        pycard_resources+=[filename]

# includes for py2exe
includes=[]
for comp in ['button','multicolumnlist','statictext', 'textfield']:
    includes += ['PythonCard.components.'+comp]
print 'includes',includes

opts = { 'py2exe': { 'includes':includes } }
print 'opts',opts
# end of py2exe stuff

setup(name='w3jdebug',
    version='0.1',
    url='http://www.wc3campaigns.net',
    author='PipeDream',
    author_email='root@127.0.0.1',
    package_dir={'blarg':'.'},
    packages=['blarg'],
    data_files=[('.',pycard_resources)],
    console=['pyw3jdebug.py'],
    options=opts
    )
