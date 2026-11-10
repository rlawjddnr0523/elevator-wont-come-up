public enum Mode {
    STANDARD(new StandardPolicy()),
    RUSH_HOUR(new RushHourPolicy()),
    ECO(new EcoPolicy());
    
    private final DispatchPolicy policy;
    
    Mode(DispatchPolicy policy) { this.policy = policy; }
    
    public DispatchPolicy policy() { return policy; }
}
