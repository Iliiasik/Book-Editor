package bookeditor.client.editor.textbox;

public class StyleParams {
    public boolean bold;
    public boolean italic;
    public boolean underline;
    public int argb;
    public float size;

    public StyleParams(boolean bold, boolean italic, boolean underline, int argb, float size) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.argb = argb;
        this.size = size;
    }
}