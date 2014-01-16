package ipse.util;


public class Color 
{
	public static int HSBtoRGB(float hue, float saturation, float brightness) 
	{
		float fr, fg, fb;

		if(saturation == 0) {
			fr = fg = fb = brightness;
		} else {
			float H = (hue - (float)Math.floor(hue)) * 6;
			int I = (int) Math.floor(H);
			float F = H - I;
			float M = brightness * (1 - saturation);
			float N = brightness * (1 - saturation * F);
			float K = brightness * (1 - saturation * (1 - F));

			switch(I) {
			case 0:
				fr = brightness; fg = K; fb = M; break;
			case 1:
				fr = N; fg = brightness; fb = M; break;
			case 2:
				fr = M; fg = brightness; fb = K; break;
			case 3:
				fr = M; fg = N; fb = brightness; break;
			case 4:
				fr = K; fg = M; fb = brightness; break;
			case 5:
				fr = brightness; fg = M; fb = N; break;
			default:
				fr = fb = fg = 0; // impossible, to supress compiler error
			}
		}

		int r = (int) (fr * 255. + 0.5);
		int g = (int) (fg * 255. + 0.5);
		int b = (int) (fb * 255. + 0.5);

		return (r << 16) | (g << 8) | b | 0xFF000000;
	}
}
