    LIBRARY IEEE;
    USE IEEE.STD_LOGIC_1164.ALL;
    USE IEEE.NUMERIC_STD.ALL;

    ENTITY ugpspi IS
        GENERIC ( USPI_SIZE : INTEGER := 24);
        PORT ( resetn : IN STD_LOGIC;
               bclk : IN STD_LOGIC;
               start : IN STD_LOGIC;
               spilen : In STD_LOGIC_VECTOR(4 DOWNTO 0);
               cpha : IN STD_LOGIC; 
               done : OUT STD_LOGIC;
               enscs0 : in STD_LOGIC;
               enscs1 : in STD_LOGIC;
               enscs2 : in STD_LOGIC;
               scs0 : out STD_LOGIC;
               scs1 : out STD_LOGIC;
               scs2 : out STD_LOGIC;
               sclk : OUT STD_LOGIC;
               sdo : OUT STD_LOGIC; 
               sdi : IN STD_LOGIC; 
               rcvData : OUT STD_LOGIC_VECTOR (USPI_SIZE-1 DOWNTO 0):=(others=>'0');
               sndData : IN STD_LOGIC_VECTOR (USPI_SIZE-1 DOWNTO 0));
               
    END ugpspi;
    ARCHITECTURE Behavioral OF ugpspi IS

    TYPE state_type IS (sidle, sstartx, sstart_lo, sclk_lo, sclk_hi, stop_hi, stop_lo);
    SIGNAL  state, next_state: state_type;
    SIGNAL sclk_i, scsq_i, sdo_i : STD_LOGIC;
    SIGNAL wr_buf : STD_LOGIC_VECTOR(USPI_SIZE-1 DOWNTO 0):=(others=>'0');
    SIGNAL rd_buf : STD_LOGIC_VECTOR(USPI_SIZE-1 DOWNTO 0):=(others=>'0');
    SIGNAL count : INTEGER RANGE 0 TO 50;
    SIGNAL DataLengthI : INTEGER RANGE 0 TO USPI_SIZE;
    CONSTANT CLK_DIV : INTEGER := 50;
    -- spi sclk = 100 MHz / (2 * CLK_DIVIDER) = 1 MHz
    SUBTYPE  ClkDiv_type IS INTEGER RANGE 0 to CLK_DIV-1;
    SIGNAL  spi_clkp : STD_LOGIC;
    BEGIN       
          rcvData <= rd_buf;
          DataLengthI<= to_integer(unsigned(spilen));
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
                    count <= USPI_SIZE-1;
                    sdo <= '0'; scs0<= '0'; scs1<= '0'; 
                    scs2<= '0';
                    sclk <= '1'; 
                    
                ELSIF spi_clkp='1' THEN
                    IF next_state=sstartx THEN  
                       count <=DataLengthI-1;
                        wr_buf(USPI_SIZE-1 downto USPI_SIZE-DataLengthI)  <= sndData(DataLengthI-1 downto 0);
                    ELSIF next_state=sclk_lo THEN
                        wr_buf(USPI_SIZE-1 downto USPI_SIZE-DataLengthI) <= wr_buf(USPI_SIZE-2 downto USPI_SIZE-DataLengthI) & '-';                     
                        rd_buf <= (rd_buf(USPI_SIZE-2 downto 0) & sdi); 
                    ELSIF next_state=sclk_hi THEN
                        count <= count - 1;
                    ELSIF next_state=stop_lo THEN
                        rd_buf <= (rd_buf(USPI_SIZE-2 downto 0) & sdi);
                    END IF;
                    state <= next_state;
                    sclk <= sclk_i;
                    scs0 <= NOT scsq_i AND enscs0;
                    scs1 <= NOT scsq_i AND enscs1;
                    scs2 <= NOT scsq_i AND enscs2;
                    sdo <= sdo_i;
                END IF;
            END IF;
        END PROCESS sseq_proc;
        
        --spi Combinational Logic
        scmb_proc: PROCESS(state, start, count, wr_buf)
        BEGIN
            next_state <= state;
            sclk_i <= '1';
            scsq_i <= '0';
            sdo_i <= '0';
            done <= '0';
            CASE state IS
                WHEN sidle =>
                    done <= '1'; scsq_i <= '1';
                    IF start='1' THEN
                        next_state <= sstartx;                      
                    END IF;
                WHEN sstartx =>
                    IF cpha='0' THEN
                    scsq_i <= '1';
                    END IF;                 
                    next_state <= sstart_lo;
                WHEN sstart_lo =>
                    IF cpha='1' THEN
                    sclk_i <= '0';
                    END IF;
                    sdo_i <= wr_buf(USPI_SIZE-1);
                    next_state <= sclk_hi;
                WHEN sclk_hi =>
                     IF cpha='0' THEN
                     sclk_i <= '0';
                     END IF;
                    sdo_i <= wr_buf(USPI_SIZE-1);
                    next_state <= sclk_lo;
                WHEN sclk_lo =>
                    IF cpha='1' THEN
                    sclk_i <= '0';
                    END IF;
                    sdo_i <= wr_buf(USPI_SIZE-1);
                    IF count=0 THEN
                        next_state <= stop_hi;
                    ELSE
                        next_state <= sclk_hi;
                    END IF;
                WHEN stop_hi =>
                    sdo_i <= wr_buf(USPI_SIZE-1);                   
                    IF cpha='0' THEN                    
                    sclk_i <= '0';
                    END IF;
                    next_state <= stop_lo;
                WHEN stop_lo =>
                    IF cpha='1' THEN
                    sclk_i <= '1';
                    END IF;  
                    next_state <= sidle;
            END CASE;
        END PROCESS scmb_proc;

    END Behavioral;