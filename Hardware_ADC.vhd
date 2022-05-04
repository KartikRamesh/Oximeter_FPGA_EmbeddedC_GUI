LIBRARY IEEE;
	USE IEEE.STD_LOGIC_1164.ALL;

	ENTITY spiadc IS
		GENERIC ( USPI_SIZE : INTEGER := 16;
				  MOSI_SIZE : INTEGER := 8);
		PORT ( resetn : IN STD_LOGIC;--
			   bclk : IN STD_LOGIC;--
			   start : IN STD_LOGIC;--
			   done : OUT STD_LOGIC;--
			   adc_cs : OUT STD_LOGIC;--
			   sclk : OUT STD_LOGIC;--
			   mosi_din : OUT STD_LOGIC; --For Read Matter--
			   miso_dout : IN STD_LOGIC; --For Write Matter--
			   sndData : IN STD_LOGIC_VECTOR (USPI_SIZE-1 DOWNTO 0);--16--
			   recData : OUT STD_LOGIC_VECTOR (USPI_SIZE-1 DOWNTO 0));--16--
			   
	END spiadc;

	ARCHITECTURE Behavioral OF spiadc IS

	TYPE state_type IS (sidle, sstart, spi_hi, spi_lo, sstop);
	SIGNAL  state, next_state: state_type;
	SIGNAL sclk_i, adc_cs_i, mosi_din_i: STD_LOGIC;
	SIGNAL wr_buf : STD_LOGIC_VECTOR(USPI_SIZE-1 DOWNTO 0);
	SIGNAL re_buf : STD_LOGIC_VECTOR(USPI_SIZE-1 DOWNTO 0);
    SIGNAL count_wr : INTEGER RANGE 0 TO MOSI_SIZE;-- to stop at 8 bits
    SIGNAL count_re : INTEGER RANGE 0 TO USPI_SIZE-1;

	CONSTANT CLK_DIV : INTEGER := 3;
	CONSTANT PHA : INTEGER := 0;
	CONSTANT CPLOL : INTEGER := 1;
	SUBTYPE  ClkDiv_type IS INTEGER RANGE 0 to CLK_DIV-1;
	SIGNAL  spi_clkp : STD_LOGIC;

	BEGIN

		
		
		-- Clock Division Logic
		clk_d : PROCESS(bclk, resetn)
		VARIABLE clkd_cnt : ClkDiv_type;
		BEGIN
			IF rising_edge(bclk) THEN
				spi_clkp <= '0';
				IF resetn= '0' THEN
					clkd_cnt := CLK_DIV - 1;
				ELSIF clkd_cnt=0 THEN
					spi_clkp <= '1';
					clkd_cnt := CLK_DIV - 1;
				ELSE
					clkd_cnt := clkd_cnt - 1;
				END IF;
			END IF;
		END PROCESS clk_d;


		-- spi sequential logic
		sseq_proc: PROCESS(bclk)
		BEGIN
			IF rising_edge(bclk) THEN
				IF resetn='0' THEN
					state <= sidle;
					count_wr <= MOSI_SIZE;
					mosi_din <= '0';
					sclk <= '1';
					adc_cs <= '1';
				ELSIF spi_clkp='1' THEN
					IF next_state=sstart THEN
						wr_buf <= sndData;
						count_wr <= MOSI_SIZE;
						count_re <= USPI_SIZE-1;
					ELSIF next_state=spi_lo and state=spi_hi THEN
						wr_buf <= wr_buf(USPI_SIZE-2 downto 0) & '-';
						IF count_wr /= 0 THEN
							count_wr <= count_wr - 1;
							count_re <= count_re - 1;
						ELSIF count_re /= 0 THEN
							count_re <= count_re - 1;
						END IF;
						
					END IF;
					state <= next_state;
					sclk <= sclk_i;
					adc_cs <= adc_cs_i;
					mosi_din <= mosi_din_i;
					recData <= re_buf;
				END IF;
			END IF;
		END PROCESS sseq_proc;
		
		--spi Combinational Logic
		scmb_proc: PROCESS(state, start, count_wr, count_re, wr_buf, miso_dout)
		BEGIN
			next_state <= state;
			sclk_i <= '1';
			adc_cs_i <= '0';
			mosi_din_i <= '0';
			done <= '0';
			CASE state IS
				WHEN sidle =>
					done <= '1';
					adc_cs_i <= '1';
					IF start='1' THEN
						next_state <= sstart;
					END IF;
				WHEN sstart =>
					next_state <= spi_lo;
				WHEN spi_lo =>
				     sclk_i <= '0';
				     IF count_wr /= 0 THEN
				     	mosi_din_i <= wr_buf(USPI_SIZE-1);
				     ELSE
				     	mosi_din_i <= '-';
					END IF;
					 re_buf <= re_buf(USPI_SIZE-2 downto 0) & miso_dout;
					 next_state <= spi_hi;
				WHEN spi_hi =>
				    IF count_wr /= 0 THEN
						mosi_din_i <= wr_buf(USPI_SIZE-1);
				     ELSE
				     	mosi_din_i <= '-';
					END IF;
					re_buf <= re_buf(USPI_SIZE-1 downto 1) & miso_dout;
					IF count_re=0 THEN
						next_state <= sstop;
					ELSE
						next_state <= spi_lo;
					END IF;
				WHEN sstop =>
					next_state <= sidle;
					adc_cs_i <= '1';
			END CASE;
		END PROCESS scmb_proc;

	END Behavioral;
