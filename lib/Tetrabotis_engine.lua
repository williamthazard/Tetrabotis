local Tetrabotis = {}
local Formatters = require 'formatters'

-- first, we'll collect all of our commands into norns-friendly ranges
local specs = {
  ["firstpitch"] = controlspec.FREQ,
  ["firstform"] = controlspec.FREQ,
  ["firstwidth"] = controlspec.AMP,
  ["firstphase"] = controlspec.PHASE,
  ["firstchaos"] = controlspec.new(-24, 24, "lin", 0, 0, ""),
  ["panone"] = controlspec.PAN,
  ["secondpitch"] = controlspec.FREQ,
  ["secondform"] = controlspec.FREQ,
  ["secondwidth"] = controlspec.AMP,
  ["secondphase"] = controlspec.PHASE,
  ["secondchaos"] = controlspec.new(-24, 24, "lin", 0, 0, ""),
  ["pantwo"] = controlspec.PAN,
  ["thirdpitch"] = controlspec.FREQ,
  ["thirdform"] = controlspec.FREQ,
  ["thirdwidth"] = controlspec.AMP,
  ["thirdphase"] = controlspec.PHASE,
  ["thirdchaos"] = controlspec.new(-24, 24, "lin", 0, 0, ""),
  ["panthree"] = controlspec.PAN,
  ["fourthpitch"] = controlspec.FREQ,
  ["fourthform"] = controlspec.FREQ,
  ["fourthwidth"] = controlspec.AMP,
  ["fourthphase"] = controlspec.PHASE,
  ["fourthchaos"] = controlspec.new(-24, 24, "lin", 0, 0, ""),
  ["panfour"] = controlspec.PAN,
  ["firstattack"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s"),
  ["secondattack"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s"),
  ["thirdattack"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s"),
  ["fourthattack"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s"),
  ["firstrelease"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s"),
  ["secondrelease"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s"),
  ["thirdrelease"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s"),
  ["fourthrelease"] = controlspec.new(0.003, 3, "exp", 0, 0.003, "s")
}

-- this table establishes an order for parameter initialization:
local param_names = {"firstpitch","firstform","firstwidth","firstphase","firstchaos","panone","secondpitch","secondform","secondwidth","secondphase","secondchaos","pantwo","thirdpitch","thirdform","thirdwidth","thirdphase","thirdchaos","panthree","fourthpitch","fourthform","fourthwidth","fourthphase","fourthchaos","panfour","firstattack","secondattack","thirdattack","fourthattack","firstrelease","secondrelease","thirdrelease","fourthrelease"}

-- initialize parameters:
function Tetrabotis.add_params()
  params:add_group("Tetrabotis",#param_names)

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
function Tetrabotis.firstbartrig(hz)
  if hz ~= nil then
    engine.firstbartrig(hz)
  end
end

function Tetrabotis.secondbartrig(hz)
  if hz ~= nil then
    engine.secondbartrig(hz)
  end
end

function Tetrabotis.thirdbartrig(hz)
  if hz ~= nil then
    engine.thirdbartrig(hz)
  end
end

function Tetrabotis.fourthbartrig(hz)
  if hz ~= nil then
    engine.fourthbartrig(hz)
  end
end

 -- we return these engine-specific Lua functions back to the host script:
return Tetrabotis
