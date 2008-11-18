
/*** sharedBlock ***/
component pt_ufixed_const is
	generic
	(
		CONST_HIGH		:	integer		:= 15;
		CONST_LOW		:	integer		:= 0;
		CONST_VALUE		:	real			:= 0.125
	) ;
	port
	(
		output			: OUT std_logic_vector (CONST_HIGH-CONST_LOW DOWNTO 0) 
	) ;
end component pt_ufixed_const;
/**/

/*** fireBlock ($genericMap, $portMap) ***/
$actorSymbol(instance): pt_ufixed_const
	GENERIC MAP ( $genericMap )
	PORT MAP ( $portMap );
/**/