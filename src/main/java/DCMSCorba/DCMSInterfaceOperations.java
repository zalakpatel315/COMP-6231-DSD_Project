package DCMSCorba;

public interface DCMSInterfaceOperations 
{
  String createTRecord (String managerID, String teacher);
  String createSRecord (String managerID, String student);
  String getRecordCount (String managerID);
  String editRecord (String managerID, String recordID, String fieldname, String newvalue);
  String transferRecord (String managerID, String recordID, String location);
  String killServer (String location);
} // interface DcmsOperations
