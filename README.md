### A lo-fi image rasterizer. What's that mean?

Input a picture, receive a lower-quality output for some artistic effect. **Codebase should currently be considered alpha.**

Video example (Output quality degraded since this relies on 1:1 pixel rendering without artifacting/compression): https://youtu.be/6jZPurgsiAQ

0 external dependencies other than Oracle JDK 8. Just compile and run.

### What's the purpose of this program?

I personally *love* the artistry of low color palettes that are used to create distinct looking art in old computer games. I think one of the standout examples of this is Loom. The purpose of this program is to achieve some semblance of that effect, for one, but to also very quickly be able to scan through and see potentially hundreds of outputs just by clicking a few buttons and adjusting some sliders.

You can achieve similar (and currently better) outputs in Photoshop by changing the mode of an image to Indexed, but the process is tedious and slow. My goal is to make that one piece of functionality really fast and easy to use.

### Some Thoughts
When editing large enough images, you are performing some operations hundreds of thousands, if not millions, of times over. At the time of writing, all my calculations are done on the CPU, and most of them are parallelized. An extremely important part of achieving speed is to remove as much decision making during the algorithm's processing as is possible to ensure good branch prediction, for one, and to make sure as much data is in the CPU cache for two. For this reason I've had to dispense with clarity in code in some respects for significant increase in speed.

At the same time, there are some "inefficiencies" that I just have to live with for choosing Java. For example, it makes lots of sense to represent ARGB channels independently as bytes. However, Java doesn't have built-in support for unsigned bytes. I instead represent the RGB channels as ints, wasting some memory, but saving a lot of implementation headache, particularly when it comes to error-diffusion. Regardless, it is my objective to reuse references when possible instead of creating new ones, to reduce the overhead of allocating memory and particularly to avoid garbage collection.

Finally, while the program should work fine for any commit I make (I won't push non-compiling code), the **codebase should be considered alpha** quality. Lots of commented code as I work through designs for correct output or increased performance in either memory or computation, a lack of bit shifts sometimes while I worked on implementation, or how DitherGrayscale.java is practically all static when it needn't be (though it made sense at the time I created the file. It's an easy fix, it just hasn't been done yet). There are no unit tests because so much is constantly being changed. Significant room for organization and clarity on the UI code. It's all on the agenda.


### Example outputs of my cat

Random Threshold
![](https://raw.githubusercontent.com/homeisfar/resources/master/output0.png)

Bayer 2x2
![](https://raw.githubusercontent.com/homeisfar/resources/master/output1.png)

Bayer 4x4
![](https://raw.githubusercontent.com/homeisfar/resources/master/output2.png)

Bayer 8x8
![](https://raw.githubusercontent.com/homeisfar/resources/master/output3.png)

Simple Threshold
![](https://raw.githubusercontent.com/homeisfar/resources/master/output4.png)

Floyd & Steinberg 16 colors (Commodore 64)
![](https://raw.githubusercontent.com/homeisfar/resources/master/output5.png)

### Resources I've relied on

http://cv.ulichney.com/papers/1993-void-cluster.pdf

http://caca.zoy.org/study/part2.html

http://bisqwit.iki.fi/story/howto/dither/jy/#Abstract

https://www.google.com/patents/US6606166

http://www.ece.ubc.ca/~irenek/techpaps/introip/manual04.html

https://en.wikipedia.org/wiki/Dither#Algorithms

https://en.wikipedia.org/wiki/Colour_look-up_table

https://en.wikipedia.org/wiki/Color_difference

https://en.wikipedia.org/wiki/RGBA_color_space

https://en.wikipedia.org/wiki/List_of_8-bit_computer_hardware_palettes
