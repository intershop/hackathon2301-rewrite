-- DEPRECATED since 7.9
-- Images of categories are no longer randomly assigned via job and this stored procedure.
-- Instead the image of a category can be assigned via back office.
-- IS-18061

CREATE OR ALTER PROCEDURE SP_UPDATE_CATEGORYIMAGES
(
  @cataloguuid NVARCHAR(30),
  @primaryviewid NVARCHAR(30)
)
AS
BEGIN
	DECLARE @noresult VARCHAR(30); -- As discussed with Carsten, as this is deprecated the conversion is not required. 
END
GO