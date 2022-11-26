local Tetrabotis = {}
local Formatters = require 'formatters'

-- first, we'll collect all of our commands into norns-friendly ranges
local specs = {
  ["rise"] = controlspec.new(0.00005, 0.03125, 'lin', 0.00001, 0.001135, 's'),
  ["fall"] = controlspec.new(0.00005, 0.03125, 'lin', 0.00001, 0.001135, 's'),
  ["chaos"] = controlspec.new(-24, 24, 'lin', 0, 0, ''),
}

-- this table establishes an order for parameter initialization:
local param_names = {"rise","fall","chaos"}

for i = 0,3 do
  table.insert(specs, "time_" .. i)
  specs["time_" .. i] = controlspec.new(0.00005, 0.03125, 'lin', 0.00001, 0.001135, 's')
  table.insert(specs, "pan_" .. i)
  specs["pan_" .. i] = controlspec.PAN
  table.insert(param_names, "time_" .. i)
  table.insert(param_names, "pan_" .. i)
end

-- initialize parameters:
function Tetrabotis.add_params()
  params:add_group("Tetrabobo",#param_names)

  for i = 1,#param_names do
    local p_name = param_names[i]
    params:add{
      type = "control",
      id = "Tetrabotis_"..p_name,
      name = p_name,
      controlspec = specs[p_name],
      formatter = util.string_starts(p_name,"pan") and Formatters.bipolar_as_pan_widget or nil,
      -- every time a parameter changes, we'll send it to the SuperCollider engine:
      action = function(x) engine[p_name](x) end
    }
  end
  
  params:bang()
end

-- a single-purpose triggering command fire a note
function Tetrabotis.trig(d, n)
  if d ~= nil then
    if n == 1 then
    engine.trig_0(d)
    elseif n == 2 then
      engine.trig_1(d)
    elseif n == 3 then
      engine.trig_2(d)
    elseif n == 4 then
      engine.trig_3(d)
    end
  end
end

 -- we return these engine-specific Lua functions back to the host script:
return Tetrabotis
