library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
port (
        -- Users to add ports here
        uSWITCH_art : in std_logic_vector ( 1 downto 0 ); 
        uPUSHB_art : in std_logic_vector ( 3 downto 0 ); 
        uLED_blu : out std_logic_vector ( 1 downto 0 );
        uLED_grn : out std_logic_vector ( 1 downto 0 );
        uLED_red : out std_logic_vector ( 1 downto 0 );
        uLED_hig : out std_logic_vector ( 3 downto 0 );
        upsclk : out std_logic;
        uadccs : out std_logic;
        upgacs : out std_logic;
        udaccs : out std_logic;
        upsmosi : out std_logic;
        upsmiso : in std_logic;
        urdon: out std_logic;
        uiron: out std_logic);
        -- User ports ends

    ---- SPI Instantiation
    COMPONENT spi is
    GENERIC ( USPI_SIZE : INTEGER := 24 );
    PORT ( resetn : in STD_LOGIC;
           bclk : in STD_LOGIC;
           cpha : in std_logic;
           spilen : in std_logic_vector(4 downto 0);
           start : in STD_LOGIC;
           done : out STD_LOGIC;
           enscs0 : in STD_LOGIC;
           enscs1 : in STD_LOGIC;
           enscs2 : in STD_LOGIC;
           scs0 : out STD_LOGIC;
           scs1 : out STD_LOGIC;
           scs2 : out STD_LOGIC;
           sclk : out STD_LOGIC;
           sdi : in STD_LOGIC;
           sdo : out STD_LOGIC;
           sndData : in STD_LOGIC_VECTOR (USPI_SIZE-1 downto 0);
           rcvData : out STD_LOGIC_VECTOR (USPI_SIZE-1 downto 0));
    end COMPONENT spi;

        ---- Signals for poxi user logic
    CONSTANT SPI_NBITS : integer := 24;
    SIGNAL  spi_start, spi_done : std_logic;
    SIGNAL rcvData : STD_LOGIC_VECTOR (SPI_NBITS-1 downto 0);
    SIGNAL spi_scs : std_logic;

-- Address decoding for reading registers
        loc_addr := axi_araddr(ADDR_LSB + OPT_MEM_ADDR_BITS downto ADDR_LSB);
        case loc_addr is
          when b"00" =>
          reg_data_out <= x"000000" & "00" & uSWITCH_art & uPUSHB_art;
        when b"01" =>
          reg_data_out <= x"000000" & "0000000" & spi_done;
        when b"10" =>
          reg_data_out <= slv_reg2;
        when b"11" =>
          reg_data_out <= x"00" & rcvData;
          when others =>
            reg_data_out  <= (others => '0');
        end case;
    end process; 
-- Add user logic here
    
    uLED_blu <= slv_reg0(9 downto 8);
    uLED_grn <= slv_reg0(7 downto 6);
    uLED_red <= slv_reg0(5 downto 4);
    uLED_hig <= slv_reg0(3 downto 0);

    urdon <= slv_reg1(0);
    uiron <= slv_reg1(1);

    poxspi : ugpspi
            GENERIC MAP ( USPI_SIZE => SPI_NBITS )
            PORT MAP ( resetn => S_AXI_ARESETN,
                       bclk => S_AXI_ACLK,
                       cpha => slv_reg2(8),
                       spilen => slv_reg2(4 downto 0),
                       start => spi_start,
                       done => spi_done,
                       enscs0 => slv_reg2(13),
                       enscs1 => slv_reg2(14),
                       enscs2 => slv_reg2(15),
                       scs0 => udaccs,
                       scs1 => upgacs,
                       scs2 => uadccs,
                       sclk => upsclk,
                       sdi => upsmiso,
                       sdo => upsmosi,
                       sndData => slv_reg3(SPI_NBITS-1 downto 0),
                       rcvData => rcvData);
    -- User logic ends

end arch_imp
