DROP TABLE PERSONNES;
DROP TABLE ETUDIANT;
DROP TABLE EMPLOYE;
DROP TABLE VILLE;
DROP TABLE PROF;
DROP SEQUENCE NumP;
DROP SEQUENCE NumEmp;
DROP SEQUENCE NumEtu;
DROP SEQUENCE NumProf;

CREATE TABLE VILLE (
NomV VARCHAR2(20) CONSTRAINT pk_V PRIMARY KEY,
BORNE_INF NUMBER(3,0) NOT NULL,
BORNE_SUP NUMBER(3,0) NOT NULL);



--PERSONNES
CREATE SEQUENCE NumP
  MINVALUE 0
  START WITH 0
  INCREMENT BY 1
  NOCACHE;

CREATE TABLE PERSONNES (
NoP Integer CONSTRAINT pk_P PRIMARY KEY,
NOMP VARCHAR2(20) NOT NULL,
PNOMP VARCHAR2(20) NOT NULL,
COMMENTAIRE VARCHAR2(200) NOT NULL,
VILLE VARCHAR2(20) NOT NULL CONSTRAINT fk_P_V REFERENCES VILLE);

INSERT INTO VILLE VALUES('Dijon',0,39);
INSERT INTO VILLE VALUES('Chenove',40,64);
INSERT INTO VILLE VALUES('Talant',65,84);
INSERT INTO VILLE VALUES('Quetigny',85,94);
INSERT INTO VILLE VALUES('Chevigny',95,98);
INSERT INTO VILLE VALUES('Bressez',99,100);
INSERT INTO VILLE VALUES('Paris',0,0);

CREATE INDEX INDEX_PERSONNES_VILLE ON PERSONNES(VILLE);

DECLARE
   CURSOR col IS SELECT NomV, BORNE_INF, BORNE_SUP FROM VILLE WHERE NOMV <> 'Paris';
   col_NomV VILLE.NomV %TYPE;
   col_BORNE_INF VILLE.BORNE_INF %TYPE;
   col_BORNE_SUP VILLE.BORNE_SUP %TYPE;
   Nom_Personne VARCHAR2(20) := '';
   Commentaire_Personne VARCHAR2(200) := '';
   PNom_Personne VARCHAR2(20) := '';
   NumVille NUMBER(3,0) := -1;
   NomVille VARCHAR2(20) := '';
BEGIN
   FOR i IN 1..10000
   LOOP
       Nom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       Commentaire_Personne := DBMS_RANDOM.string('L', TRUNC(DBMS_RANDOM.value(40,50)));
       PNom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       NumVille := TRUNC(DBMS_RANDOM.value(0,100));
       OPEN col;
       LOOP
          FETCH col INTO col_NomV, col_BORNE_INF, col_BORNE_SUP;
	  EXIT WHEN (col%NOTFOUND);
            IF NumVille >= col_BORNE_INF AND NumVille <= col_BORNE_SUP THEN
		NomVille := col_NomV;
            END IF;
       END LOOP;
       CLOSE col;
       INSERT INTO PERSONNES VALUES(NumP.nextval,Nom_Personne,PNom_Personne,Commentaire_Personne, NomVille);
   END LOOP;
END;
/ 

--EMPLOYE
CREATE SEQUENCE NumEmp
  MINVALUE 0
  START WITH 0
  INCREMENT BY 1
  NOCACHE;

CREATE TABLE EMPLOYE (
NoEmp Integer CONSTRAINT pk_Emp PRIMARY KEY,
NOMEmp VARCHAR2(20) NOT NULL,
PNOMEmp VARCHAR2(20) NOT NULL,
COMMENTAIRE VARCHAR2(200) NOT NULL,
VILLE VARCHAR2(20) NOT NULL CONSTRAINT fk_Emp_V REFERENCES VILLE);

CREATE INDEX INDEX_EMPLOYE_VILLE ON EMPLOYE(VILLE);

DECLARE
   CURSOR col IS SELECT NomV, BORNE_INF, BORNE_SUP FROM VILLE WHERE NOMV <> 'Paris';
   col_NomV VILLE.NomV %TYPE;
   col_BORNE_INF VILLE.BORNE_INF %TYPE;
   col_BORNE_SUP VILLE.BORNE_SUP %TYPE;
   Nom_Personne VARCHAR2(20) := '';
   Commentaire_Personne VARCHAR2(200) := '';
   PNom_Personne VARCHAR2(20) := '';
   NumVille NUMBER(3,0) := -1;
   NomVille VARCHAR2(20) := '';
BEGIN
   FOR i IN 1..500
   LOOP
       Nom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       Commentaire_Personne := DBMS_RANDOM.string('L', TRUNC(DBMS_RANDOM.value(40,50)));
       PNom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       NumVille := TRUNC(DBMS_RANDOM.value(0,100));
       OPEN col;
       LOOP
          FETCH col INTO col_NomV, col_BORNE_INF, col_BORNE_SUP;
	  EXIT WHEN (col%NOTFOUND);
            IF NumVille >= col_BORNE_INF AND NumVille <= col_BORNE_SUP THEN
		NomVille := col_NomV;
            END IF;
       END LOOP;
       CLOSE col;
       INSERT INTO EMPLOYE VALUES(NumEmp.nextval,Nom_Personne,PNom_Personne,Commentaire_Personne, NomVille);
   END LOOP;
END;
/ 
-- pour avoir des blos différents
DECLARE
   CURSOR col IS SELECT NomV, BORNE_INF, BORNE_SUP FROM VILLE WHERE NOMV <> 'Paris' AND NOMV <> 'DIJON';
   col_NomV VILLE.NomV %TYPE;
   col_BORNE_INF VILLE.BORNE_INF %TYPE;
   col_BORNE_SUP VILLE.BORNE_SUP %TYPE;
   Nom_Personne VARCHAR2(20) := '';
   Commentaire_Personne VARCHAR2(200) := '';
   PNom_Personne VARCHAR2(20) := '';
   NumVille NUMBER(3,0) := -1;
   NomVille VARCHAR2(20) := '';
BEGIN
   FOR i IN 1..300
   LOOP
       Nom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       Commentaire_Personne := DBMS_RANDOM.string('L', TRUNC(DBMS_RANDOM.value(40,50)));
       PNom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       NumVille := TRUNC(DBMS_RANDOM.value(0,100));
       OPEN col;
       LOOP
          FETCH col INTO col_NomV, col_BORNE_INF, col_BORNE_SUP;
	  EXIT WHEN (col%NOTFOUND);
            IF NumVille >= col_BORNE_INF AND NumVille <= col_BORNE_SUP THEN
		NomVille := col_NomV;
            END IF;
       END LOOP;
       CLOSE col;
       INSERT INTO EMPLOYE VALUES(NumEmp.nextval,Nom_Personne,PNom_Personne,Commentaire_Personne, NomVille);
   END LOOP;
END;
/ 

--ETUDIANT
CREATE SEQUENCE NumEtu
  MINVALUE 0
  START WITH 0
  INCREMENT BY 1
  NOCACHE;

CREATE TABLE ETUDIANT (
NoEtu Integer CONSTRAINT pk_Etu PRIMARY KEY,
NOMEtu VARCHAR2(20) NOT NULL,
PNOMEtu VARCHAR2(20) NOT NULL,
COMMENTAIRE VARCHAR2(200) NOT NULL,
VILLE VARCHAR2(20) NOT NULL CONSTRAINT fk_Etu_V REFERENCES VILLE);

CREATE INDEX INDEX_ETUDIANT_VILLE ON ETUDIANT(VILLE);

DECLARE
   CURSOR col IS SELECT NomV, BORNE_INF, BORNE_SUP FROM VILLE WHERE NOMV <> 'Paris';
   col_NomV VILLE.NomV %TYPE;
   col_BORNE_INF VILLE.BORNE_INF %TYPE;
   col_BORNE_SUP VILLE.BORNE_SUP %TYPE;
   Nom_Personne VARCHAR2(20) := '';
   Commentaire_Personne VARCHAR2(200) := '';
   PNom_Personne VARCHAR2(20) := '';
   NumVille NUMBER(3,0) := -1;
   NomVille VARCHAR2(20) := '';
BEGIN
   FOR i IN 1..2000
   LOOP
       Nom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       Commentaire_Personne := DBMS_RANDOM.string('L', TRUNC(DBMS_RANDOM.value(40,50)));
       PNom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       NumVille := TRUNC(DBMS_RANDOM.value(0,100));
       OPEN col;
       LOOP
          FETCH col INTO col_NomV, col_BORNE_INF, col_BORNE_SUP;
	  EXIT WHEN (col%NOTFOUND);
            IF NumVille >= col_BORNE_INF AND NumVille <= col_BORNE_SUP THEN
		NomVille := col_NomV;
            END IF;
       END LOOP;
       CLOSE col;
       INSERT INTO ETUDIANT VALUES(NumEtu.nextval,Nom_Personne,PNom_Personne,Commentaire_Personne, NomVille);
   END LOOP;
END;
/ 


--PROF
CREATE SEQUENCE NumProf
  MINVALUE 0
  START WITH 0
  INCREMENT BY 1
  NOCACHE;

CREATE TABLE PROF (
NoProf Integer CONSTRAINT pk_Prof PRIMARY KEY,
NOMProf VARCHAR2(20) NOT NULL,
PNOMProf VARCHAR2(20) NOT NULL,
COMMENTAIRE VARCHAR2(200) NOT NULL,
VILLE VARCHAR2(20) NOT NULL CONSTRAINT fk_Prof_V REFERENCES VILLE);

CREATE INDEX INDEX_PROF_VILLE ON PROF(VILLE);

DECLARE
   CURSOR col IS SELECT NomV, BORNE_INF, BORNE_SUP FROM VILLE;
   col_BORNE_INF VILLE.BORNE_INF %TYPE;
   col_BORNE_SUP VILLE.BORNE_SUP %TYPE;
   Nom_Personne VARCHAR2(20) := '';
   Commentaire_Personne VARCHAR2(200) := '';
   PNom_Personne VARCHAR2(20) := '';
   NumVille NUMBER(3,0) := -1;
   NomVille VARCHAR2(20) := '';
BEGIN
   FOR i IN 1..500
   LOOP
       Nom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       Commentaire_Personne := DBMS_RANDOM.string('L', TRUNC(DBMS_RANDOM.value(40,50)));
       PNom_Personne := DBMS_RANDOM.string('L',TRUNC(DBMS_RANDOM.value(5,21)));
       NumVille := TRUNC(DBMS_RANDOM.value(0,100));
       INSERT INTO PROF VALUES(NumProf.nextval,Nom_Personne,PNom_Personne,Commentaire_Personne, 'Paris');
   END LOOP;
END;
/ 


ALTER SYSTEM FLUSH buffer_cache;
ALTER SYSTEM FLUSH shared_pool;
EXEC DBMS_STATS.GATHER_SCHEMA_STATS('bb111272', CASCADE => TRUE);

