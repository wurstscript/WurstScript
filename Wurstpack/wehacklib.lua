function removequotes(text)
	if string.len(text) > 1 then
		if string.sub(text, 1, 1) == "\"" and string.sub(text, string.len(text), string.len(text)) == "\"" then
			return string.sub(text, 2, string.len(text) - 1)
		else
			return text
		end
	elseif string.len(text) == 1 and string.sub(text, 1, 1) ~= "\"" then
		return text
	else
		return ""
	end
end

function strjoin(delimiter, list)
  local len = table.getn(list)
  if len == 0 then return "" end
  local string = list[1]
  for i = 2, len do string = string .. delimiter .. list[i] end
  return string
end

function argsplit(text)
	local list = {}
	local pos = 1
	local last = " "
	local inquote = false
	local current = ""
	while pos <= string.len(text) do
		if string.sub(text, pos, pos) == " " then
			if inquote then
				current = current .. string.sub(text, pos, pos)
			elseif string.len(current) > 0 then
				table.insert(list, current)
				current = ""
			end
		elseif string.sub(text, pos, pos) == "\"" then
			if not inquote or last ~= "\\" then
				inquote = not inquote
			end
			current = current .. string.sub(text, pos, pos)
		else
			current = current .. string.sub(text, pos, pos)
		end
		last = string.sub(text, pos, pos)
		pos = pos + 1
	end
	if string.len(current) > 0 then
		table.insert(list, current)
	end
	return list
end

function strsplit(delimiter, text)
  local list = {}
  local pos = 1
  if delimiter == "" then -- this would result in endless loops
    return list
  end
  while true do
    local first, last = string.find(text, delimiter, pos)
    if first then -- found?
      table.insert(list, string.sub(text, pos, first-1))
      pos = last+1
    else
      table.insert(list, string.sub(text, pos))
      break
    end
  end
  return list
end

function onlyfile(filename)
  local pos = 1
  while true do
    local startPos, endPos = string.find(filename, "\\", pos)
    if startPos then
        pos = endPos+1
    else
        return string.sub(filename, pos)
    end
  end
  return filename
end

function argsjoin(target, list)
  local len = table.getn(list)
  if len == 0 then 
    return "" 
  end
  if len == 1 then
    return " \""..list[1].."\" \""..target..onlyfile(list[1]).."\""
  end
  if string.sub(list[1], string.len(list[1])) ~= "\\" then
    list[1] = list[1].."\\"
  end
  local string = ""
  for i = 2, len do 
    string = string.." \""..list[1]..list[i].."\" \""..target..list[i].."\""
  end
  return string
end

function fileargsjoin(list)
  local len = table.getn(list)
  if len == 0 then 
    return "" 
  end
  if len == 1 then
    return " \""..list[1].."\""
  end
  if string.sub(list[1], string.len(list[1])) ~= "\\" then
    list[1] = list[1].."\\"
  end
  local string = ""
  for i = 2, len do 
    string = string.." \""..list[1]..list[i].."\""
  end
  return string
end

menuentries = {}
-- this function is run by wehack.dll
function clickevent(id)
	menuentries[id]:Click()
end

MenuEntry = {}
MenuEntry_mt = { __index = MenuEntry }
function MenuEntry:create()
	local o = {}
	setmetatable( o, MenuEntry_mt )
	return o
end
function MenuEntry:Init(o,menu,name,cb)
	o.menu = menu
	o.name = name
	o.cb = cb or function () end
	o.id = wehack.addmenuentry(menu,name,"clickevent")
	menuentries[o.id] = o
	return o
end
function MenuEntry:New(menu,name,cb)
	local o = MenuEntry:create()
	MenuEntry:Init(o,menu,name,cb)
	return o
end
function MenuEntry:Click()
	if self.cb then
		self.cb(self)
	end
end

function inheritsFrom(base)
	local newclass = {}
	local classmt = { __index = newclass }
	function newclass:create()
		local o = {}
		setmetatable(o,classmt)
		return o
	end
	if base then
		setmetatable(newclass,{__index = base} )
	end
	return newclass
end

-- Toggleable Menu Entry with saved state in registry
TogMenuEntry = inheritsFrom(MenuEntry)
function TogMenuEntry:Init(o,menu,name,cb,default)
	MenuEntry:Init(o,menu,name,cb)
	local state = grim.getregpair(confregpath,name)
	if state == "on" or (state ~= "off" and default) then
		o.checked = true
		grim.setregstring(confregpath,name,"on") -- set in case this is the first time we're running this
		wehack.checkmenuentry(o.menu,o.id,1)
		if o.cb then
			o.cb(o)
		end
	else
		wehack.checkmenuentry(o.menu,o.id,0)
		o.checked = false
	end

	return o
end
function TogMenuEntry:redraw()
	if self.checked then
		wehack.checkmenuentry(self.menu,self.id,1)
		grim.setregstring(confregpath,self.name,"on")
	else
		wehack.checkmenuentry(self.menu,self.id,0)
		grim.setregstring(confregpath,self.name,"off")
	end
end
function TogMenuEntry:New(menu,name,cb,default)
	o = TogMenuEntry:create()
	TogMenuEntry:Init(o,menu,name,cb,default)
	return o
end
function TogMenuEntry:Click()
	self.checked = not self.checked

	self.redraw(self)
	if self.cb then
		self.cb(self)
	end
end
