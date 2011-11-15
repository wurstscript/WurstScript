nopause = true                  -- no pausing in single player
profiling = false               -- when on, call LogJASSCalls

-- bytecodetrace enables either bytecode tracer
bytecodetrace = false
-- bytecodetracefast writes to files each 64kB long
-- if false, writes forever to one file
bytecodetracefast = true
-- bytecodeflush specifies whether to flush if bytecodetracefast is off, use this to pick up crashes
-- does nothing if bytecodetracefast is on
bytecodeflush = false

btonerr = true                  -- include back trace on errors
btonerrlen = 5                  -- number of functions to trace back

debugger = true                 -- enable debugging engine

-- handle_monitor = true        -- enable handle monitor
-- handle_monitor_add(0x100015) -- add a handle to watch for

detect_missed_null = true       -- report when a function doesn't null a local variable which is a handle with value > 0x100000 and isn't a function argument (i.e. potential leak)
