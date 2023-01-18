-- DEPRECATED since 7.9
-- Images of categories are no longer randomly assigned via job and this stored procedure.
-- Instead the image of a category can be assigned via back office.
-- IS-18061

create or replace
PROCEDURE SP_UPDATE_CATEGORYIMAGES
(
  cataloguuid IN VARCHAR2,
  primaryviewid IN VARCHAR2
)
AS
  in_catalogid Catalog.uuid%TYPE := cataloguuid;
  primaryView VARCHAR(100) := primaryviewid;
  k INTEGER;
  subcatuuid CatalogCategory.uuid%TYPE;
  t VARCHAR(256);
  u PRODUCT.UUID%TYPE;
  created_imageref_uuid IMAGEREFERENCE.UUID%TYPE;
BEGIN
    -- check the primary view
    IF primaryView IS NULL THEN
        primaryView := 'front';
    END IF;

  FOR k IN (SELECT uuid FROM catalogcategory WHERE domainid IN (SELECT catalogdomainid FROM catalog WHERE uuid = in_catalogid))
    LOOP
        EXIT WHEN k.uuid = NULL;

        BEGIN

          -- Get the image from a catalogcategory's random product
          BEGIN
              SELECT PRODUCT_UUID into u
              FROM
              (
                SELECT p.uuid AS PRODUCT_UUID, ipa.productuuid, COUNT(it.id) as COUNTER
                FROM product p,
                     ieproductcategoryassignment iepca,
                     imageproductassignment ipa,
                     imagereference ir,
                     imagetype it,
                     imageview iv
                WHERE
                    iepca.categoryid = k.uuid
                AND iepca.productid = p.uuid
                AND p.uuid = ipa.productuuid
                AND ipa.imagereferenceuuid = ir.uuid
                AND ir.imagetypeuuid = it.uuid
                AND ir.imageviewuuid = iv.uuid
                AND p.onlineflag = 1
                AND p.availableflag = 1
                AND iv.id = primaryView
                AND ir.imagebasename IS NOT NULL
                GROUP BY ipa.productuuid, p.uuid
                ORDER BY COUNTER desc, dbms_random.value
              )
              WHERE ROWNUM = 1;
              
              EXCEPTION
                -- If the image productref is null, then either the catalogcategory doesn't
                -- have products or no products with the needed props (onlineflag=1, etc.)
                WHEN NO_DATA_FOUND THEN

                   -- Then try to select the image of a subcategory's random product
                   SELECT PRODUCT_UUID into u
                    FROM
                    (
                       SELECT p.uuid AS PRODUCT_UUID, ipa.productuuid, COUNT(it.id) as COUNTER
                        FROM product p,
                             ieproductcategoryassignment iepca,
                             imageproductassignment ipa,
                             imagereference ir,
                             imagetype it,
                             imageview iv
                        WHERE
                            iepca.categoryid IN (SELECT uuid FROM catalogcategory WHERE parentcategoryid = k.uuid AND onlineflag = 1)
                        AND iepca.productid = p.uuid
                        AND p.uuid = ipa.productuuid
                        AND ipa.imagereferenceuuid = ir.uuid
                        AND ir.imagetypeuuid = it.uuid
                        AND ir.imageviewuuid = iv.uuid
                        AND p.onlineflag = 1
                        AND p.availableflag = 1
                        AND iv.id = primaryView
                        AND ir.imagebasename IS NOT NULL
                        GROUP BY ipa.productuuid, p.uuid
                        ORDER BY COUNTER desc, dbms_random.value
                    )
                    WHERE ROWNUM = 1;
            END;

          IF u IS NULL THEN
              EXIT;
            END IF;

        -- delete old entries
        DELETE FROM IMAGEREFERENCE WHERE UUID IN (SELECT imagereferenceuuid FROM IMAGECATEGORYASSIGNMENT WHERE CATALOGCATEGORYUUID = k.uuid);
        DELETE FROM IMAGECATEGORYASSIGNMENT WHERE CATALOGCATEGORYUUID = k.uuid;
            
           -- create image reference for category use image reference values of the product
           FOR r IN (SELECT ir.uuid FROM imagereference ir, imageproductassignment ipa WHERE ir.uuid = ipa.imagereferenceuuid AND ipa.productuuid = u)

           LOOP
                BEGIN

                    SELECT SF_CREATE_UUID('A', 24) INTO created_imageref_uuid FROM DUAL;

                    -- create image reference
                    INSERT INTO imagereference (IMAGEVIEWUUID, IMAGETYPEUUID, IMAGEBASENAME, UUID, DOMAINID, OCA, LASTMODIFIED)
                        SELECT ir.IMAGEVIEWUUID, ir.IMAGETYPEUUID, ir.IMAGEBASENAME, created_imageref_uuid, ir.DOMAINID, 0, UTCTIMESTAMP()
                        FROM imagereference ir WHERE uuid = r.uuid;

                    -- create image category assignment for category k
                    INSERT INTO IMAGECATEGORYASSIGNMENT (IMAGEREFERENCEUUID, DOMAINID, CATALOGCATEGORYUUID, OCA, LASTMODIFIED)
                        VALUES(created_imageref_uuid, (SELECT DOMAINID FROM catalogcategory WHERE uuid = k.uuid), k.uuid, 0, UTCTIMESTAMP());

                END;

           END LOOP;

        EXCEPTION
         WHEN NO_DATA_FOUND THEN
          NULL;
        END;

    END LOOP;

  COMMIT;

END SP_UPDATE_CATEGORYIMAGES;
/

show errors;
/
