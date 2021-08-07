package DCMSCorba;

public final class DCMSInterfaceHolder implements org.omg.CORBA.portable.Streamable
{
  public DCMSCorba.DCMSInterface value = null;

  public DCMSInterfaceHolder ()
  {
  }

  public DCMSInterfaceHolder (DCMSCorba.DCMSInterface initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = DCMSCorba.DCMSInterfaceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    DCMSCorba.DCMSInterfaceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return DCMSCorba.DCMSInterfaceHelper.type ();
  }

}
