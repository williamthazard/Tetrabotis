Engine_Tetrabotis : CroneEngine {

  //Decimate
  var srate=48000, sdepth=32;

  //Saturate
  var crossover=1400,
      distAmount=15, //1-500
      lowbias=0.04, //0.01 - 1
      highbias=0.12, //0.01 - 1
      hissAmount=0.5, //0.0 - 1.0
      cutoff=11500;

  var <saturator;

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {
  
  //Tetrabate
  var time = [0.001, 0.001, 0.001, 0.001];
  var pan = [0, 0, 0, 0];
  var params;

SynthDef("Bar", {
  var rise = \rise.kr(0.001);
  var fall = \fall.kr(0.001);
  var i = \i.ir(0);
  var time = \time.kr(0.001 ! 4);
  var t1 = Select.kr(i, time);
  var t2 = Select.kr((i - 1) % 4, time);
  var chaos = \chaos.kr(1);

  var amp_env = Env.perc(\atk.kr(1),\rel.kr(1),1).kr(2);
  var fm = LFTri.ar((2*t2 + rise + fall).reciprocal, mul:chaos);
  var retrigger = Trig1.ar(Pulse.ar((1/(rise + fall + (2 * t1))) + fm, 0.5, 1), 4000.reciprocal);
	var shape = Env.perc(((rise + t1).reciprocal + fm).reciprocal,
		((fall + t1).reciprocal + fm).reciprocal, 1);
  var signal = SineShaper.ar(EnvGen.ar(shape, retrigger),mul:amp_env);
  signal = Pan2.ar(signal, \pan.kr(0));
  Out.ar(0, signal);
}).add;

    ~tf =  Env([-0.7, 0, 0.7], [1,1], [8,-8]).asSignal(1025);
    ~tf = ~tf + (
          Signal.sineFill(
            1025,
            (0!3) ++ [0,0,1,1,0,1].scramble,
            {rrand(0,2pi)}!9
        )/10;
      );
    ~tf = ~tf.normalize;
    ~tfBuf = Buffer.loadCollection(context.server, ~tf.asWavetableNoWrap);

    SynthDef(\Saturator, { |inL, inR, out, srate=48000, sdepth=32, crossover=1400, distAmount=15, lowbias=0.04, highbias=0.12, hissAmount=0.5, cutoff=11500|
      var input = Decimator.ar(SoundIn.ar([0,1]),srate, sdepth);
      var crossAmount = 50;

      var lpf = LPF.ar(
        input,
        crossover + crossAmount,
        1
      ) * lowbias;

      var hpf = HPF.ar(
        input,
        crossover - crossAmount,
        1
      ) * highbias;

      var beforeHiss = Mix.new([
        Mix.new([lpf,hpf]),
        HPF.ar(Mix.new([PinkNoise.ar(0.001), Dust.ar(5,0.002)]), 2000, hissAmount)
      ]);

      var compressed = Compander.ar(beforeHiss, input,
          thresh: 0.2,
          slopeBelow: 1,
          slopeAbove: 0.3,
          clampTime:  0.001,
          relaxTime:  0.1,
      );
      var shaped = Shaper.ar(~tfBuf, compressed  * distAmount);

      var afterHiss = HPF.ar(Mix.new([PinkNoise.ar(1), Dust.ar(5,1)]), 2000, 1);

      var duckedHiss = Compander.ar(afterHiss, input,
          thresh: 0.4,
          slopeBelow: 1,
          slopeAbove: 0.2,
          clampTime: 0.01,
          relaxTime: 0.1,
      ) * 0.5 * hissAmount;

      var morehiss = Mix.new([
        duckedHiss,
        Mix.new([lpf * (1 / lowbias) * (distAmount/10), shaped])
      ]);

      var limited = Limiter.ar(Mix.new([
        input * 0.5,
        morehiss
      ]), 0.9, 0.01);

      Out.ar(out, MoogFF.ar(
        limited,
        cutoff,
        1
      ));
    }).add;

    context.server.sync;

    saturator = Synth.new(\Saturator, [
      \inL, context.in_b[0].index,
      \inR, context.in_b[1].index,
      \out, context.out_b.index,
      \srate, 48000,
      \sdepth, 32,
      \crossover, 1400, //500-9k
      \distAmount, 15, //1-500
      \lowbias, 0.04, //0.01 - 1
      \highbias, 0.12, //0.01 - 1
      \hissAmount, 0.2, //0.0 - 1.0
      \cutoff, 11500],
    context.xg);

		params = Dictionary.newFrom([
\rise, 0.001,
\fall, 0.001,
\chaos, 0
		]);

params.keysDo({ arg key;
			this.addCommand(key, "f", { arg msg;
				params[key] = msg[1]
			});
		});


4.do({arg i;
	this.addCommand("trig_"++i, "f", { arg msg;
		Synth.new("Bar", [\time, time, \i, i, \pan, pan[i], \atk, msg[1], \rel, msg[1]] ++ params.getPairs)
		});
	this.addCommand("time_"++i, "f", { arg msg;
		time[i] = msg[1]
	});
	this.addCommand("pan_"++i, "f", { arg msg;
		pan[i] = msg[1]
	});
});	

    this.addCommand("srate", "i", {|msg|
      saturator.set(\srate, msg[1]);
    });

    this.addCommand("sdepth", "f", {|msg|
      saturator.set(\sdepth, msg[1]);
    });

    this.addCommand("crossover", "i", {|msg|
      saturator.set(\crossover, msg[1]);
    });

    this.addCommand("distAmount", "i", {|msg|
      saturator.set(\distAmount, msg[1]);
    });

    this.addCommand("lowbias", "f", {|msg|
      saturator.set(\lowbias, msg[1]);
    });

    this.addCommand("highbias", "f", {|msg|
      saturator.set(\highbias, msg[1]);
    });

    this.addCommand("hissAmount", "f", {|msg|
	     var amp = msg[1]*0.1;
       if(amp>0.001, {amp = amp.linexp(0.001, 1, 0.001, 0.25)});
	     saturator.set(\hissAmount, amp);
    });
  }

  free {
    saturator.free;
  }
}
