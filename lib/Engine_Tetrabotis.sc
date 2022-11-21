Engine_Tetrabotis : CroneEngine {
  //Tetrabate
	var params;
	var firstbar;
	var secondbar;
	var thirdbar;
	var fourthbar;
	var outBus;
	var firstfmbus;
	var secondfmbus;
	var thirdfmbus;
	var fourthfmbus;
	var endOfChain;

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

SynthDef("ColorLimiter",
		{arg input;
			Out.ar(
				0,
				In.ar(input).dup;
			);
		}).add;

Server.default.sync;

outBus = Bus.audio;

firstfmbus = Bus.audio;

secondfmbus = Bus.audio;

thirdfmbus = Bus.audio;

fourthfmbus = Bus.audio;

endOfChain = Synth.new("ColorLimiter",[\input,outBus]);

NodeWatcher.register(endOfChain);

SynthDef("barone",
		{   arg out,
			firstfmbus,
			fourthfmbus,
			firstchaos = 0,
			firstpitch = 440,
			firstwidth = 0.75,
			firstform = 440,
			firstphase = 0,
			firstattack = 0.3,
			firstrelease = 1,
			panone = 0;

			var firstbar_env = Env.perc(
					attackTime: firstattack,
					releaseTime: firstrelease,
					level: 0.01
				).kr(doneAction:2);

			var first_signal = Pan2.ar(
					SineShaper.ar(
						FormantTriPTR.ar(firstpitch + (
							firstchaos * In.ar(fourthfmbus)),
						firstform,
						firstwidth,
						firstphase),
						mul: firstbar_env),
					panone);

				Out.ar(
					out,
					first_signal;
				);

				Out.ar(
					firstfmbus,
					first_signal;
				)

		}).add;

SynthDef("bartwo",
		{   arg out,
			firstfmbus,
			secondfmbus,
			secondchaos = 0,
			secondpitch = 440,
			secondwidth = 0.75,
			secondform = 440,
			secondphase = 0,
			secondattack = 0.3,
			secondrelease = 1,
			pantwo = 0;

			var secondbar_env = Env.perc(
					attackTime: secondattack,
					releaseTime: secondrelease,
					level: 0.01
				).kr(doneAction:2);

			var second_signal = Pan2.ar(
					SineShaper.ar(
						FormantTriPTR.ar(secondpitch + (
							secondchaos * In.ar(firstfmbus)),
						secondform,
						secondwidth,
						secondphase),
						mul: secondbar_env),
					pantwo);

				Out.ar(
					out,
					second_signal;
				);

				Out.ar(
					secondfmbus,
					second_signal;
				)

		}).add;

SynthDef("barthree",
		{   arg out,
			secondfmbus,
			thirdfmbus,
			thirdchaos = 0,
			thirdpitch = 440,
			thirdwidth = 0.75,
			thirdform = 440,
			thirdphase = 0,
			thirdattack = 0.3,
			thirdrelease = 1,
			panthree = 0;

			var thirdbar_env = Env.perc(
					attackTime: thirdattack,
					releaseTime: thirdrelease,
					level: 0.01
				).kr(doneAction:2);

			var third_signal = Pan2.ar(
					SineShaper.ar(
						FormantTriPTR.ar(thirdpitch + (
							thirdchaos * In.ar(secondfmbus)),
						thirdform,
						thirdwidth,
						thirdphase),
						mul: thirdbar_env),
					panthree);

				Out.ar(
					out,
					third_signal;
				);

				Out.ar(
					thirdfmbus,
					third_signal;
				)

		}).add;

SynthDef("barfour",
		{   arg out,
			thirdfmbus,
			fourthfmbus,
			fourthchaos = 0,
			fourthpitch = 440,
			fourthwidth = 0.75,
			fourthform = 440,
			fourthphase = 0,
			fourthattack = 0.3,
			fourthrelease = 1,
			panfour = 0;

			var fourthbar_env = Env.perc(
					attackTime: fourthattack,
					releaseTime: fourthrelease,
					level: 0.01
				).kr(doneAction:2);

			var fourth_signal = Pan2.ar(
					SineShaper.ar(
						FormantTriPTR.ar(fourthpitch + (
							fourthchaos * In.ar(thirdfmbus)),
						fourthform,
						fourthwidth,
						fourthphase),
						mul: fourthbar_env),
					panfour);

				Out.ar(
					out,
					fourth_signal;
				);

				Out.ar(
					fourthfmbus,
					fourth_signal;
				)

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
			\firstpitch, 440,
			\firstform, 440,
			\firstwidth, 0.75,
			\firstphase, 0,
			\panone, 0,
			\firstattack, 0.3,
			\firstrelease, 1,
			\secondpitch, 440,
			\secondform, 440,
			\secondwidth, 0.75,
			\secondphase, 0,
			\pantwo, 0,
			\secondattack, 0.3,
			\secondrelease, 1,
			\thirdpitch, 440,
			\thirdform, 440,
			\thirdwidth, 0.75,
			\thirdphase, 0,
			\panthree, 0,
			\thirdattack, 0.3,
			\thirdrelease, 1,
			\fourthpitch, 440,
			\fourthform, 440,
			\fourthwidth, 0.75,
			\fourthphase, 0,
			\panfour, 0,
			\fourthattack, 0.3,
			\fourthrelease, 1,
			\firstchaos, 0,
			\secondchaos, 0,
			\thirdchaos, 0,
			\fourthchaos, 0
		]);

		params.keysDo({ arg key;
			this.addCommand(key, "f", { arg msg;
				params[key] = msg[1];
			});
		});


		this.addCommand("firstbartrig", "f", { arg msg;
			firstbar = Synth.before(
				endOfChain,
				"barone",
				[\out,outBus,
				\firstfmbus,firstfmbus,
				\fourthfmbus,fourthfmbus,
				\firstpitch,msg[1]]
				++ params.getPairs)
		});

		this.addCommand("secondbartrig", "f", { arg msg;
			secondbar = Synth.before(
				endOfChain,
				"bartwo",
				[\out,outBus,
				\firstfmbus,firstfmbus,
				\secondfmbus,secondfmbus,
				\secondpitch, msg[1]]
				++ params.getPairs)
		});

		this.addCommand("thirdbartrig", "f", { arg msg;
			thirdbar = Synth.before(
				endOfChain,
				"barthree",
				[\out,outBus,
				\secondfmbus,secondfmbus,
				\thirdfmbus,thirdfmbus,
				\thirdpitch, msg[1]]
				++ params.getPairs)
		});

		this.addCommand("fourthbartrig", "f", { arg msg;
			fourthbar = Synth.before(
				endOfChain,
				"barfour",
				[\out,outBus,
				\thirdfmbus,thirdfmbus,
				\fourthfmbus,fourthfmbus,
				\fourthpitch, msg[1]]
				++ params.getPairs)
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
		endOfChain.free;
			outBus.free;
		    firstfmbus.free;
		    secondfmbus.free;
		    thirdfmbus.free;
		    fourthfmbus.free;
  }
}
